package com.team01.backend.domain.comment.controller;

import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.dto.CommentResponseDto;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/post/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDto>> writeComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto reqDto){

        User user = new User();

        CommentResponseDto resDto = commentService.writeComment(
                postId, reqDto, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(resDto));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto) {

        User user = new User();
        CommentResponseDto response = commentService.updateComment(
                commentId, requestDto, user);

        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }
}
