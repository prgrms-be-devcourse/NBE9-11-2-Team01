package com.team01.backend.domain.category.dto;

import com.team01.backend.domain.category.entity.Category;

import java.time.LocalDateTime;

public record CategoryResponseDto(
        long id,
        long boardId,
        String name,
        LocalDateTime createdAt
) {
    public CategoryResponseDto(Category category){
        this(
            category.getId(),
            category.getBoardId(),
            category.getName(),
            category.getCreatedAt()
        );
    }
}
