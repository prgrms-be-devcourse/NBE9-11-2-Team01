package com.team01.backend.domain.post.service;

import com.team01.backend.domain.post.batch.HotPostDetector;
import com.team01.backend.domain.post.dto.PostLikeResponseDto;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.entity.PostLike;
import com.team01.backend.domain.post.repository.PostLikeRepository;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final HotPostDetector hotPostDetector;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public PostLikeResponseDto toggleLike(Long postId, String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없어요"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if(hotPostDetector.isHot(postId)) {
            return toggleLikeWithRedis(postId, user, post);
        }
        else{
            return toggleLikeWithDB(postId, user, post);
        }
    }

    private PostLikeResponseDto toggleLikeWithRedis(Long postId, User loginUser, Post post) {
        String userKey = "like:post" + postId + ":user:" + loginUser.getId();
        String countKey = "like:post" + postId + ":count";

        boolean alreadyLiked = Boolean.TRUE.equals(redisTemplate.hasKey(userKey));

        if(alreadyLiked){
            redisTemplate.delete(userKey);
            redisTemplate.opsForValue().decrement(countKey);
        }else{
            redisTemplate.opsForValue().set(userKey, "1");
            redisTemplate.opsForValue().increment(countKey);
        }

        String countStr = redisTemplate.opsForValue().get(countKey);
        int likeCount = countStr != null ?Integer.parseInt(countStr) : 0;
        return new PostLikeResponseDto(alreadyLiked, likeCount);
    }

    private PostLikeResponseDto toggleLikeWithDB(Long postId, User loginUser, Post post) {
        Optional<PostLike> existing = postLikeRepository.findByUserIdAndPostId(loginUser.getId(), postId);

        if(existing.isPresent()){
            postLikeRepository.delete(existing.get());
            postRepository.decreaseLikeCount(postId);
            int likeCount = postLikeRepository.countByPostId(postId);
            return new PostLikeResponseDto(false, likeCount);
        }
        else{
            postLikeRepository.save(new PostLike(loginUser, post));
            postRepository.increaseLikeCount(postId);
            int likeCount = postLikeRepository.countByPostId(postId);
            return new PostLikeResponseDto(true, likeCount);
        }
    }
}
