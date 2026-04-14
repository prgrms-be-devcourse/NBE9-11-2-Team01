package com.team01.backend.domain.category.controller;

import com.team01.backend.domain.category.dto.CategoryResponseDto;
import com.team01.backend.domain.category.service.CategoryService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    record CategoryCreateReq(
            @NotNull(message = "게시판이 없습니다")
            Long boardId,

            @NotNull(message = "이름이 없습니다")
            @Size(min=2, message = "게시판 이름은 2자 이상이어야 합니다")
            String name
    ){}

    @Transactional
    @PostMapping
    ResponseEntity<ApiResponse<CategoryResponseDto>>createCategory(
            @RequestBody @Valid CategoryCreateReq req
    ){
        CategoryResponseDto categoryResponseDto = categoryService.create(req.boardId, req.name);

        return ResponseEntity.ok(ApiResponse.ofSuccess(categoryResponseDto));
    }
}
