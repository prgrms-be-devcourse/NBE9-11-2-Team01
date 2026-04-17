package com.team01.backend.domain.post.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PostPageResponseDto(
        List<PostResponseDto> posts,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext
) {
    public static PostPageResponseDto from(Page<PostResponseDto> page) {
        return new PostPageResponseDto(
                page.getContent(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}