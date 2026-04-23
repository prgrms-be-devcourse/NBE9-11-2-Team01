package com.team01.backend.domain.post.dto;

public record PostLikeResponseDto(
        boolean liked,
        int likeCount
) {}
