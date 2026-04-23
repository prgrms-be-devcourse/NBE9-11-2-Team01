package com.team01.backend.domain.category.controller;

import com.team01.backend.domain.category.dto.CategoryCreateResponseDto;
import com.team01.backend.domain.category.dto.CategoryResponseDto;
import com.team01.backend.domain.category.service.CategoryService;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 카테고리 관리", description = "관리자 카테고리 관리 관련 API")
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
    @Operation(summary = "카테고리 생성", description = "카테고리를 생성합니다. 관리자 권한에서만 가능합니다.")
    @PostMapping
    ResponseEntity<ApiResponse<CategoryCreateResponseDto>>createCategory(
            @RequestBody @Valid CategoryCreateReq req
    ){
        CategoryCreateResponseDto categoryCreateResponseDto = categoryService.create(req.boardId, req.name);

        return ResponseEntity.ok(ApiResponse.ofSuccess(categoryCreateResponseDto));
    }

    record CategoryUpdateReq(
            @NotNull(message = "이름이 없습니다")
            @Size(min=2, message = "카테고리 이름은 2자 이상이어야 합니다")
            String name
    ){}
    @Operation(summary = "카테고리 수정", description = "카테고리를 수정합니다. 관리자 권한에서만 가능합니다.")
    @PutMapping("/{categoryId}")
    ResponseEntity<ApiResponse<CategoryCreateResponseDto>>updateCategory(
            @PathVariable long categoryId,
            @RequestBody @Valid CategoryUpdateReq req
    ){
        // 카테고리가 있는 게시판은 변경하지 않고, 이름만 수정
        CategoryCreateResponseDto categoryResponseDto = categoryService.update(categoryId, req.name);

        return ResponseEntity.ok(ApiResponse.ofSuccess(categoryResponseDto));
    }

    @Operation(summary = "카테고리 조회", description = "전체 카테고리를 조회합니다. 관리자 권한에서만 가능합니다.")
    @GetMapping
    ResponseEntity<ApiResponse<List<CategoryResponseDto>>>viewCategory(){
        List<CategoryResponseDto> categories = categoryService.list();
        return ResponseEntity.ok(ApiResponse.ofSuccess(categories));
    }

}
