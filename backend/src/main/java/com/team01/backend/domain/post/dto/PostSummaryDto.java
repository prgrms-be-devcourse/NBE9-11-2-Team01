package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostSummaryDto(
        Long id,
        String title,
        Long boardId,
        String boardName,
        Long categoryId,
        String categoryName,
        Long authorId,
        String authorNickname,
        int likeCount,      // 좋아요 수 추가
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public PostSummaryDto(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getBoard().getId(),
                post.getBoard().getName(),
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
