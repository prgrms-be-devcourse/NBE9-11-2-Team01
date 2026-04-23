package com.team01.backend.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequestDto (

        @NotBlank(message = "댓글 내용을 입력해주세요")
        @Size(min = 1, max = 300, message = "댓글은 1자 이상 500자 이하로 입력해주세요")
        String content,
        Long parentId
        ){ }
