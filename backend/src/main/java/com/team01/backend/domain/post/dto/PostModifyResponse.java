package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostModifyResponse (
        Long id,
        String title,
        String content,
        Long boardId,
        String boardName,
        Long categoryId,
        String categoryName,
        Long authorId,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
){
    public PostModifyResponse(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getBoard().getId(),
                post.getBoard().getName(),
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
