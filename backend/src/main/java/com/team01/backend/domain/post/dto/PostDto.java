package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDto (
    Long id,
    String title,
    String content,
    //Long authorId,
    //String authorName,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
){
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                //post.getAuthor().getId(),
                //post.getAuthor().getName(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
