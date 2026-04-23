package com.team01.backend.domain.category.dto;

import com.team01.backend.domain.category.entity.Category;

import java.time.LocalDateTime;

public record CategoryCreateResponseDto(
        long id,
        long boardId,
        String name,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public CategoryCreateResponseDto(Category category){
        this(
                category.getId(),
                category.getBoardId(),
                category.getName(),
                category.getCreatedAt(),
                category.getModifiedAt()
        );
    }
}
