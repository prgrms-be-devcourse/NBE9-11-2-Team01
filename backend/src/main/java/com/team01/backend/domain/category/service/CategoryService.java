package com.team01.backend.domain.category.service;

import com.team01.backend.domain.board.service.BoardService;
import com.team01.backend.domain.category.dto.CategoryCreateResponseDto;
import com.team01.backend.domain.category.dto.CategoryResponseDto;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BoardService boardService;

    @Transactional
    public CategoryCreateResponseDto create(Long boardId, String name) {

        if(!boardService.existsById(boardId)){ //boardId에 해당하는 게시판이 없다면 예외처리
            throw new EntityNotFoundException();
        }
        if(categoryRepository.existsByBoardIdAndName(boardId, name)){
            throw new IllegalArgumentException("중복된 이름입니다");
        }
        Category category = new Category(boardId, name);
        categoryRepository.save(category);
        return new CategoryCreateResponseDto(category);
    }

    public long count() {
        return categoryRepository.count();
    }

    @Transactional
    public CategoryCreateResponseDto update(long categoryId, String name) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(EntityNotFoundException::new);

        if(categoryRepository.existsByBoardIdAndName(category.getBoardId(), name)){ //게시판 별 이름 중복 체크
            throw new IllegalArgumentException("중복된 이름입니다");
        }

        category.update(name);
        categoryRepository.save(category);

        return new CategoryCreateResponseDto(category);
    }

    public List<CategoryResponseDto> list() {
        return categoryRepository.findAll().stream()
                                .map(CategoryResponseDto::new)
                                .toList();

    }
}
