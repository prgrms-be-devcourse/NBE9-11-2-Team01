package com.team01.backend.domain.category.controller;

import com.team01.backend.domain.category.dto.CategoryResponseDto;
import com.team01.backend.domain.category.service.CategoryService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    record CategoryCreateReq(
            @NotNull(message = "게시판이 없습니다")
            Long boardId,

            @NotNull(message = "이름이 없습니다")
            @Size(min=2, message = "카테고리 이름은 2자 이상이어야 합니다")
            String name
    ){}

    @PostMapping
    ResponseEntity<ApiResponse<CategoryResponseDto>>createCategory(
            @RequestBody @Valid CategoryCreateReq req
    ){
        CategoryResponseDto categoryResponseDto = categoryService.create(req.boardId, req.name);

        return ResponseEntity.ok(ApiResponse.ofSuccess(categoryResponseDto));
    }

    record CategoryUpdateReq(
            @NotNull(message = "이름이 없습니다")
            @Size(min=2, message = "카테고리 이름은 2자 이상이어야 합니다")
            String name
    ){}

    @PutMapping("/{categoryId}")
    ResponseEntity<ApiResponse<CategoryResponseDto>>updateCategory(
            @PathVariable long categoryId,
            @RequestBody @Valid CategoryUpdateReq req
    ){
        // 카테고리가 있는 게시판은 변경하지 않고, 이름만 수정
        CategoryResponseDto categoryResponseDto = categoryService.update(categoryId, req.name);

        return ResponseEntity.ok(ApiResponse.ofSuccess(categoryResponseDto));
    }


}
