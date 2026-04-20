package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.dto.*;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.service.PostService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시판별 글 목록 조회
    @GetMapping("/boards/{boardId}/posts")
    public ResponseEntity<ApiResponse<PostPageResponseDto>> getPostsByBoardId(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        if (page < 1) throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        if (keyword != null && keyword.length() > 50) throw new IllegalArgumentException("검색어는 50자 이하이어야 합니다.");

        PostPageResponseDto posts = postService.getPostsByBoardId(boardId, page, keyword, categoryId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(posts));
    }

    // 게시판별, 카테고리별 글 목록 조회
    @GetMapping("/boards/{boardId}/categories/{categoryId}/posts")
    public ResponseEntity<ApiResponse<List<PostSummaryDto>>> getPostsByCategory(
            @PathVariable("boardId") Long boardId,
            @PathVariable("categoryId") Long categoryId
    ) {
        List<PostSummaryDto> posts = postService.getPostsByBoardAndCategory(boardId, categoryId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(posts));
    }


    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        PostDetailResponseDto post = postService.getPostById(postId, email);
        return ResponseEntity.ok(ApiResponse.ofSuccess(post));
    }


    record PostWriteReqBody(
            @Size(min = 2, message = "제목은 2자 이상이어야 합니다.")
            @NotBlank(message = "제목은 공백일 수 없습니다.")
            String title,

            @Size(min = 2, message = "내용은 2자 이상이어야 합니다.")
            @NotBlank(message = "내용은 공백일 수 없습니다.")
            String content,

            @NotNull(message = "게시판 선택은 필수입니다.")
            Long boardId,

            @NotNull(message = "카테고리 선택은 필수입니다.")
            Long categoryId
    ){
    }

    // 글 작성 api
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostWriteResponse>> write(
            @RequestBody @Valid PostWriteReqBody reqBody,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // 비로그인 사용자에 대한 예외 처리
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요한 서비스입니다.");
        }

        Post post = postService.write(
                userDetails.getUsername(),
                reqBody.title,
                reqBody.content,
                reqBody.boardId,
                reqBody.categoryId
        );

        long postsCount = postService.count();

        return ResponseEntity.ok(
                ApiResponse.ofSuccess(
                        new PostWriteResponse(post, postsCount)
                )
        );
    }

    record PostModifyReqBody(
            @Size(min = 2, message = "제목은 2자 이상이어야 합니다.")
            @NotBlank(message = "제목은 공백일 수 없습니다.")
            String title,

            @Size(min = 2, message = "내용은 2자 이상이어야 합니다.")
            @NotBlank(message = "내용은 공백일 수 없습니다.")
            String content,

            @NotNull(message = "카테고리 선택은 필수입니다.")
            Long categoryId
    ) {
    }

    // 글 수정 api
    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostModifyResponse>> modify(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid PostModifyReqBody reqBody,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요한 서비스입니다.");
        }

        Post post = postService.modify(
                postId,
                userDetails.getUsername(),
                reqBody.title(),
                reqBody.content(),
                reqBody.categoryId);

        return ResponseEntity.ok(
                ApiResponse.ofSuccess(new PostModifyResponse(post))
        );
    }

    // 글 삭제 api
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요한 서비스입니다.");
        }

        postService.delete(postId, userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.ofSuccess(null)
        );
    }
}
