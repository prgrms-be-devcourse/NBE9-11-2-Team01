package com.team01.backend.domain.comment.controller;

import com.team01.backend.domain.comment.dto.CommentDeleteResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @Valid @RequestBody CommentRequestDto reqDto,
            @AuthenticationPrincipal UserDetails userDetails){

        CommentResponseDto resDto = commentService.writeComment(
                postId, reqDto, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(resDto));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponseDto resDto = commentService.updateComment(
                commentId, requestDto, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(resDto));
    }


        // COMMENT-04 댓글(답글) 삭제 — DELETE, 소프트 딜리트(서비스에서 isDeleted 처리)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentDeleteResponseDto>> deleteComment(@PathVariable Long commentId) {

        // 임시 — 나중에 @AuthenticationPrincipal 로 교체
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없어요"));

        CommentDeleteResponseDto body = commentService.deleteComment(commentId, user);
        return ResponseEntity.ok(ApiResponse.ofSuccess(body));
    }
}
