package com.team01.backend.domain.comment.dto;

public record CommentLikeToggleResponseDto(
        Long commentId,
        int likeCount,
        boolean liked
) {
}
