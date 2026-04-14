package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDetailResponseDto(
        Long id,
        Long boardId,
        // TODO: categoryId 추가 예정
        String title,
        String content,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public PostDetailResponseDto(Post post) {
        this(
                post.getId(),
                post.getBoardId(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
