package com.team01.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostWriteResponse (
        Long id,
        String title,
        String content,
        Long boardId,
        String boardName,
        Long categoryId,
        String categoryName,
        Long authorId,
        String authorNickname,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime modifiedAt,

        long postsCount // 기존 응답에 없던 postsCount를 추가하여 평탄화
){
    public PostWriteResponse(Post post, long postsCount) {
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
                post.getModifiedAt(),
                postsCount
        );
    }
}
