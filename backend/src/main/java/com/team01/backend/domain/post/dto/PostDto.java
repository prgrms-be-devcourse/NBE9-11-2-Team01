package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDto (
    Long id,
    String title,
    String content,
    Long boardId,
    String boardName,
    Long categoryId,
    String categoryName,
//    Long authorId,
//    String authorName,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
){
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getBoard().getId(),
                post.getBoard().getName(),
                post.getCategory().getId(),
                post.getCategory().getName(),
//                post.getAuthor().getId(),
//                post.getAuthor().getName(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
