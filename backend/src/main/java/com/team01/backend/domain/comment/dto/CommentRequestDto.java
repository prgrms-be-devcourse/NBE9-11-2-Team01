package com.team01.backend.domain.comment.dto;

public record CommentRequestDto (
        String content,
        Long parentId
        ){ }
