package com.team01.backend.global.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisInitializer implements ApplicationRunner {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // init, count, users 키 전체 삭제
        Set<String> allKeys = redisTemplate.keys("like:post:*");
        if (allKeys != null && !allKeys.isEmpty()) {
            redisTemplate.delete(allKeys);
            log.info("=== 서버 시작 시 Redis like:post:* 키 {} 개 삭제 완료", allKeys.size());
        } else {
            log.info("=== 삭제할 키 없음");
        }
    }
}