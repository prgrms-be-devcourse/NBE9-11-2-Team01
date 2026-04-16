package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponseDto(
        Long id,
        String title,
        String author,
        Long categoryId,
        String categoryName,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public PostResponseDto(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getNickname(),
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
