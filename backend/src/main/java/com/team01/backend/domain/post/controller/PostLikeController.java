package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.dto.PostLikeResponseDto;
import com.team01.backend.domain.post.service.PostLikeService;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시글 좋아요", description = "게시글 좋아요 관련 API")
@RestController
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 좋아요 토글", description = "게시글 좋아요를 누르거나 취소합니다. 한 번 누르면 좋아요, 다시 누르면 취소됩니다.")
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<ApiResponse<PostLikeResponseDto>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostLikeResponseDto response = postLikeService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }

    @Hidden
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<ApiResponse<Integer>> getLikes(
            @PathVariable Long postId) {

        int likeCount = postLikeService.getLikeCount(postId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(likeCount));
    }
}
