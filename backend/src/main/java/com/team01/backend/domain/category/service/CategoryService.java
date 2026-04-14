package com.team01.backend.domain.category.service;

import com.team01.backend.domain.category.dto.CategoryResponseDto;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponseDto create(Long boardId, String name) {
        Category category = new Category(boardId, name);
        categoryRepository.save(category);
        return new CategoryResponseDto(category);
    }
}
