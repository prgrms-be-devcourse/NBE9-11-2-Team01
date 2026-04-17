package com.team01.backend.domain.post.batch;

import com.team01.backend.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeSyncBatch {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;

    @Scheduled(fixedDelay = 300000)
    public void syncLikesToDB() {
        log.info("=== 게시글 좋아요 배치 동기화 시작");

        Set<String> hotKeys = redisTemplate.keys("hot:post:*");
        if (hotKeys == null || hotKeys.isEmpty()) {
            log.info("=== 동기화할 인기 게시글 없음");
            return;
        }

        for (String hotKey : hotKeys) {
            Long postId = Long.parseLong(hotKey.replace("hot:post:", ""));
            String countKey = "like:post:" + postId + ":count";
            String countStr = redisTemplate.opsForValue().get(countKey);

            if (countStr == null) continue;

            int likeCount = Integer.parseInt(countStr);
            postRepository.updateLikeCount(postId, likeCount);
            log.info("=== 게시글 {} liked {} 로 DB 업데이트", postId, likeCount);

            redisTemplate.delete(hotKey);
            redisTemplate.delete(countKey);
            log.info("=== 게시글 {} Redis 플래그 삭제 완료", postId);
        }

        log.info("=== 게시글 좋아요 배치 동기화 완료");
    }
}