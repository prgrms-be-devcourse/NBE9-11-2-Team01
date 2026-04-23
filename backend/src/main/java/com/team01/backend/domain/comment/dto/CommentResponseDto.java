package com.team01.backend.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team01.backend.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        String content,
        String author,
        int likeCount,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime modifiedAt
) {
    // Comment 엔티티를 받아서 record 만드는 정적 팩토리 메서드
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getNickname(),
                comment.getLikeCount(),
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
