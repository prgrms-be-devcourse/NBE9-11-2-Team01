package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.dto.PostLikeResponseDto;
import com.team01.backend.domain.post.service.PostLikeService;
import com.team01.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<ApiResponse<PostLikeResponseDto>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostLikeResponseDto response = postLikeService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }

    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<ApiResponse<Integer>> getLikes(
            @PathVariable Long postId) {

        int likeCount = postLikeService.getLikeCount(postId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(likeCount));
    }
}
