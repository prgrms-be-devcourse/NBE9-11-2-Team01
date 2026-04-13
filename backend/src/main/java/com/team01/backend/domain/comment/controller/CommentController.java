package com.team01.backend.domain.comment.controller;

import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.dto.CommentResponseDto;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
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
}
