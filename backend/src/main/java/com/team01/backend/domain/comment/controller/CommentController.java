package com.team01.backend.domain.comment.controller;

import com.team01.backend.domain.comment.dto.CommentReadResponseDto;
import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.dto.CommentResponseDto;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import com.team01.backend.global.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    // COMMENT-02 댓글(답글) 조회
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentReadResponseDto>>> getComments(@PathVariable Long postId) {
        List<CommentReadResponseDto> list = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(list));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDto>> writeComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto reqDto){

        // 임시 — 나중에 @AuthenticationPrincipal 로 교체
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없어요"));


        CommentResponseDto resDto = commentService.writeComment(
                postId, reqDto, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(resDto));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto) {

        // 임시 — 나중에 @AuthenticationPrincipal 로 교체
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없어요"));

        CommentResponseDto response = commentService.updateComment(
                commentId, requestDto, user);

        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }
}
