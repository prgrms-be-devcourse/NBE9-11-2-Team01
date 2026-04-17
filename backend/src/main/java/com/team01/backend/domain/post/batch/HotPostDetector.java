package com.team01.backend.domain.post.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotPostDetector {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int HOT_THRESHOLD = 50;
    private static final long WINDOW_SECONDS = 60;

    public boolean isHot(Long postId) {
        String rateKey = "rate:post:" + postId;
        String hotKey = "hot:post:" + postId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(hotKey))) {
            log.info("=== 게시글 {} hot 플래그 유지 → Redis 처리", postId);
            return true;
        }

        Long count = redisTemplate.opsForValue().increment(rateKey);

        if (count != null && count == 1) {
            redisTemplate.expire(rateKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        log.info("=== 게시글 {} 요청 횟수: {} / 처리방식: {}",
                postId, count,
                count >= HOT_THRESHOLD ? "Redis" : "NativeQuery");

        if (count != null && count >= HOT_THRESHOLD) {
            redisTemplate.opsForValue().set(hotKey, "1");
            redisTemplate.expire(hotKey, 300, TimeUnit.SECONDS);
            return true;
        }

        return false;
    }
}