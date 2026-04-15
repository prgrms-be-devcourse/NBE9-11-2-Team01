package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponseDto(
        Long id,
        String title,
        String author,
        CategoryInfo category,
        int likeCount,
        LocalDateTime createdAt
) {
    public record CategoryInfo(Long id, String name) {}

    public PostResponseDto(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getNickname(),
                new CategoryInfo(post.getCategory().getId(), post.getCategory().getName()),
                post.getLikeCount(),
                post.getCreatedAt()
        );
    }
}
