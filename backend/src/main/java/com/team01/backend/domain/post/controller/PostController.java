package com.team01.backend.domain.post.controller;


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
}
