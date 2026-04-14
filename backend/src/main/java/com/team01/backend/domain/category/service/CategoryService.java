package com.team01.backend.domain.category.service;

import com.team01.backend.domain.board.service.BoardService;
import com.team01.backend.domain.category.dto.CategoryResponseDto;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BoardService boardService;

    @Transactional
    public CategoryResponseDto create(Long boardId, String name) {

        if(!boardService.existsById(boardId)){ //boardId에 해당하는 게시판이 없다면 예외처리
            throw new EntityNotFoundException();
        }

        Category category = new Category(boardId, name);
        categoryRepository.save(category);
        return new CategoryResponseDto(category);
    }

    public long count() {
        return categoryRepository.count();
    }

    @Transactional
    public CategoryResponseDto update(long categoryId, String name) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(EntityNotFoundException::new);
        category.update(name);
        categoryRepository.save(category);

        return new CategoryResponseDto(category);
    }
}
