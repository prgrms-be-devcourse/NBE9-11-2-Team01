//package com.team01.backend.domain.post.batch;
//
//import com.team01.backend.domain.post.repository.PostRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Set;
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class PostLikeSyncBatch {
//
//    private final RedisTemplate<String, String> redisTemplate;
//    private final PostRepository postRepository;
//
//    @Scheduled(fixedDelay = 60000)
//    @Transactional
//    public void syncLikesToDB() {
//        log.info("=== 게시글 좋아요 배치 동기화 시작");
//
//        Set<String> countKeys = redisTemplate.keys("like:post:*:count");
//        if (countKeys == null || countKeys.isEmpty()) {
//            log.info("=== 동기화할 데이터 없음");
//            return;
//        }
//
//        for (String countKey : countKeys) {
//            String postId = countKey.replace("like:post:", "").replace(":count", "");
//            String countStr = redisTemplate.opsForValue().get(countKey);
//            if (countStr == null) continue;
//
//            int likeCount = Integer.parseInt(countStr);
//
//            // ✅ likeCount만 DB 업데이트 (post_likes 건드리지 않음)
//            postRepository.updateLikeCount(Long.parseLong(postId), likeCount);
//            log.info("=== 게시글 {} likeCount {} 업데이트", postId, likeCount);
//        }
//
//        log.info("=== 게시글 좋아요 배치 동기화 완료");
//    }
//}
