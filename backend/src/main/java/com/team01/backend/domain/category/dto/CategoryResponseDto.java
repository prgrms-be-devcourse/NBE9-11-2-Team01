package com.team01.backend.domain.category.dto;

import com.team01.backend.domain.category.entity.Category;

public record CategoryResponseDto(
        long id,
        long boardId,
        String name
) {
    public CategoryResponseDto(Category category){
        this(
            category.getId(),
            category.getBoardId(),
            category.getName()
        );
    }
}
