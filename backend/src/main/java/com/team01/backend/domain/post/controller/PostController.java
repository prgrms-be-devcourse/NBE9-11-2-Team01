package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.dto.PostDetailResponseDto;
import com.team01.backend.domain.post.dto.PostResponseDto;
import com.team01.backend.domain.post.service.PostService;
import com.team01.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시판별 글 목록 조회
    @GetMapping("/boards/{boardId}/posts")
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getPostsByBoardId(
            @PathVariable Long boardId
    ) {
        List<PostResponseDto> posts = postService.getPostsByBoardId(boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(posts));
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostById(
            @PathVariable Long postId
            // TODO: 인증 구현 후 추가
            // User user
    ) {
        // TODO: 로그인한 사용자만 접근 가능하도록 제한 필요
        PostDetailResponseDto post = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(post));
    }
}
