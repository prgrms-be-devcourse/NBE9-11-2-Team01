package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponseDto(
        Long id,
        String title,
        String author,
        int likeCount,
        LocalDateTime createdAt
) {
    public PostResponseDto(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getNickname(),
                post.getLikeCount(),
                post.getCreatedAt()
        );
    }
}
