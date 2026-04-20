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

    private static final DefaultRedisScript<Long> TOGGLE_SCRIPT;

    static {
        TOGGLE_SCRIPT = new DefaultRedisScript<>();
        TOGGLE_SCRIPT.setScriptText("""
                if redis.call('exists', KEYS[1]) == 1 then
                    redis.call('del', KEYS[1])
                    local count = redis.call('decr', KEYS[2])
                    if count < 0 then
                        redis.call('set', KEYS[2], '0')
                    end
                    return 0
                else
                    redis.call('set', KEYS[1], '1')
                    redis.call('incr', KEYS[2])
                    return 1
                end
                """);
        TOGGLE_SCRIPT.setResultType(Long.class);
    }

    @Transactional
    public PostLikeResponseDto toggleLike(Long postId, String email) {
        log.info("=== toggleLike 호출 - postId: {}, email: {}", postId, email);

        User user = findUser(email);
        Post post = findPost(postId);

        initRedisIfAbsent(postId);
        boolean liked = executeToggle(postId, user.getId());
        syncPostLikesToDB(liked, user, post, postId);

        int likeCount = getLikeCountFromRedis(postId);
        log.info("=== 좋아요 결과 - liked: {}, likeCount: {}", liked, likeCount);
        return new PostLikeResponseDto(liked, likeCount);
    }

    // ✅ 유저 조회
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없어요"));
    }

    // ✅ 게시글 조회
    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
    }

    // ✅ Redis countKey 초기화 (없을 때만)
    private void initRedisIfAbsent(Long postId) {
        String countKey = "like:post:" + postId + ":count";
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(countKey, String.valueOf(
                        postLikeRepository.countByPostId(postId)));

        if (Boolean.TRUE.equals(isNew)) {
            syncUserKeysToRedis(postId);
        }
    }

    // ✅ 기존 좋아요 유저 Redis 동기화
    private void syncUserKeysToRedis(Long postId) {
        postLikeRepository.findByPost_Id(postId).forEach(pl -> {
            String uKey = "like:post:" + postId + ":user:" + pl.getUser().getId();
            redisTemplate.opsForValue().setIfAbsent(uKey, "1");
        });
    }

    // ✅ Lua 스크립트로 원자적 토글
    private boolean executeToggle(Long postId, Long userId) {
        String userKey = "like:post:" + postId + ":user:" + userId;
        String countKey = "like:post:" + postId + ":count";
        Long result = redisTemplate.execute(TOGGLE_SCRIPT, List.of(userKey, countKey));
        return result != null && result == 1;
    }

    // ✅ post_likes DB 즉시 반영
    private void syncPostLikesToDB(boolean liked, User user, Post post, Long postId) {
        if (liked) {
            if (postLikeRepository.findByUserIdAndPostId(user.getId(), postId).isEmpty()) {
                postLikeRepository.save(new PostLike(user, post));
            }
        } else {
            postLikeRepository.deleteByUserIdAndPostId(user.getId(), postId);
        }
    }

    // ✅ Redis에서 likeCount 조회
    private int getLikeCountFromRedis(Long postId) {
        String countKey = "like:post:" + postId + ":count";
        String countStr = redisTemplate.opsForValue().get(countKey);
        return countStr != null ? Math.max(0, Integer.parseInt(countStr)) : 0;
    }

    @Transactional(readOnly = true)
    public List<PostLike> getLikes(Long postId) {
        return postLikeRepository.findByPost_Id(postId);
    }
}


