package com.team01.backend.domain.post.service;

import com.team01.backend.domain.post.dto.PostLikeResponseDto;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.entity.PostLike;
import com.team01.backend.domain.post.repository.PostLikeRepository;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // ─────────────────────────────────────────────────────────────
    // Redis Key 규칙
    //   like:post:{postId}:count   → 좋아요 수 (String)
    //   like:post:{postId}:users   → 좋아요 유저 ID 집합 (Set)
    //   like:post:{postId}:init    → 초기화 완료 플래그 (String "1")
    // ─────────────────────────────────────────────────────────────

    private static final String PREFIX = "like:post:";

    /**
     * Lua 스크립트: 초기화 + 토글을 하나의 원자적 명령으로 처리
     *
     * KEYS[1] = initKey   (like:post:{id}:init)
     * KEYS[2] = countKey  (like:post:{id}:count)
     * KEYS[3] = usersKey  (like:post:{id}:users)
     * ARGV[1] = userId
     * ARGV[2] = dbCount   (초기화 시 DB에서 조회한 카운트)
     * ARGV[3] = dbUserIds (초기화 시 DB에서 조회한 유저 ID 쉼표 구분)
     *
     * 반환값: 1 = 좋아요 추가, 0 = 좋아요 취소
     */
    private static final DefaultRedisScript<Long> INIT_AND_TOGGLE_SCRIPT;

    static {
        INIT_AND_TOGGLE_SCRIPT = new DefaultRedisScript<>();
        INIT_AND_TOGGLE_SCRIPT.setScriptText(
                "if redis.call('exists', KEYS[1]) == 0 then\n" +
                        "    redis.call('set', KEYS[1], '1')\n" +
                        "    redis.call('set', KEYS[2], ARGV[2])\n" +
                        "    if ARGV[3] ~= '' then\n" +
                        "        local ids = {}\n" +
                        "        for id in string.gmatch(ARGV[3], '[^,]+') do\n" +
                        "            table.insert(ids, id)\n" +
                        "        end\n" +
                        "        redis.call('sadd', KEYS[3], unpack(ids))\n" +
                        "    end\n" +
                        "end\n" +
                        "local isMember = redis.call('sismember', KEYS[3], ARGV[1])\n" +
                        "if isMember == 1 then\n" +
                        "    redis.call('srem', KEYS[3], ARGV[1])\n" +
                        "    local count = redis.call('decr', KEYS[2])\n" +
                        "    if count < 0 then redis.call('set', KEYS[2], '0') end\n" +
                        "    return 0\n" +
                        "else\n" +
                        "    redis.call('sadd', KEYS[3], ARGV[1])\n" +
                        "    redis.call('incr', KEYS[2])\n" +
                        "    return 1\n" +
                        "end"
        );
        INIT_AND_TOGGLE_SCRIPT.setResultType(Long.class);
    }

    // ─────────────────────────────────────────────────────────────
    //  좋아요 토글 (메인 API)
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public PostLikeResponseDto toggleLike(Long postId, String email) {
        log.info("=== toggleLike 호출 - postId: {}, email: {}", postId, email);

        User user = findUser(email);
        Post post = findPost(postId); // 검증과 동시에 Post 객체 확보
        Long boardId = post.getBoard().getId(); // 게시판 ID 확보

        // DB 초기값 조회 (Redis에 없을 때만 실제 사용됨)
        long dbCount = postLikeRepository.countByPostId(postId);
        String dbUserIds = buildUserIdsString(postId);

        // Redis: 초기화 + 토글 원자적 실행
        Long result = redisTemplate.execute(
                INIT_AND_TOGGLE_SCRIPT,
                List.of(initKey(postId), countKey(postId), usersKey(postId)),
                String.valueOf(user.getId()),
                String.valueOf(dbCount),
                dbUserIds
        );
        boolean liked = (result != null && result == 1L);

        // DB: post_likes 이력 + posts.like_count 동시 반영
        syncToDB(liked, user, postId, boardId);

        int likeCount = getLikeCount(postId);
        log.info("=== 좋아요 결과 - liked: {}, likeCount: {}", liked, likeCount);
        return new PostLikeResponseDto(liked, likeCount);
    }

    // ─────────────────────────────────────────────────────────────
    //  좋아요 수 조회 — Redis 우선, Cold Start 시 DB fallback
    // ─────────────────────────────────────────────────────────────

    public int getLikeCount(Long postId) {
        String key = countKey(postId);

        String countStr = redisTemplate.opsForValue().get(key);

        findPost(postId);

        if (countStr != null) {
            return Math.max(0, Integer.parseInt(countStr));
        }

        int dbCount = postRepository.findById(postId)
                .map(Post::getLikeCount)
                .orElse(0);
        log.debug("=== redisValue: {}", countStr);
        return dbCount;
    }

    // ─────────────────────────────────────────────────────────────
    //  특정 유저의 좋아요 여부 확인
    // ─────────────────────────────────────────────────────────────

    public boolean isLikedByUser(Long postId, Long userId) {
        Boolean isMember = redisTemplate.opsForSet()
                .isMember(usersKey(postId), String.valueOf(userId));
        if (isMember != null) return isMember;
        return Boolean.TRUE.equals(isMember) ||
                postLikeRepository.findByUserIdAndPostId(userId, postId).isPresent();

    }

    // ─────────────────────────────────────────────────────────────
    //  좋아요 목록 조회 (DB 기준 이력)
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PostLike> getLikes(Long postId) {
        return postLikeRepository.findByPost_Id(postId);
    }

    // ─────────────────────────────────────────────────────────────
    //  내부 헬퍼
    // ─────────────────────────────────────────────────────────────

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없어요"));
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
    }

    /**
     * DB의 좋아요 유저 목록을 쉼표 구분 문자열로 반환
     * Lua 초기화 시 Redis Set에 삽입할 초기값으로 사용
     */
    private String buildUserIdsString(Long postId) {
        List<PostLike> likes = postLikeRepository.findByPost_Id(postId);
        if (likes.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (PostLike pl : likes) {
            if (sb.length() > 0) sb.append(',');
            sb.append(pl.getUser().getId());
        }
        return sb.toString();
    }

    /**
     * post_likes 이력 + posts.like_count 동시 반영
     *
     * posts.like_count는 increaseLikeCount / decreaseLikeCount로
     * DB 자체 연산(+1, -1)을 사용 → 동시 요청에서도 덮어쓰기 없이 안전
     */
    private void syncToDB(boolean liked, User user, Long postId, Long boardId) {
        if (liked) {

            postLikeRepository.mergeInsert(user.getId(), postId);
            postRepository.increaseLikeCount(postId);

            // 좋아요 수 변경 시 캐시 무효화
            evictTop5Cache(boardId);
        } else {
            int deleted = postLikeRepository.deleteByUserIdAndPostId(user.getId(), postId);
            if (deleted > 0) {
                postRepository.decreaseLikeCount(postId);

                // 좋아요 수 변경 시 캐시 무효화
                evictTop5Cache(boardId);
            }

        }
    }

    // 캐시 무효화 로직
    private void evictTop5Cache(Long boardId) {
        String cacheKey = "top5:board:" + boardId;
        redisTemplate.delete(cacheKey);
        log.info("좋아요 변경으로 인한 캐시 무효화 완료: {}", cacheKey);
    }

    // ─────────────────────────────────────────────────────────────
    //  Redis Key 생성
    // ─────────────────────────────────────────────────────────────

    private String countKey(Long postId)  { return PREFIX + postId + ":count"; }
    private String usersKey(Long postId)  { return PREFIX + postId + ":users"; }
    private String initKey(Long postId)   { return PREFIX + postId + ":init";  }
}

