package com.team01.backend.domain.comment.controller;

import com.team01.backend.domain.comment.dto.CommentDeleteResponseDto;
import com.team01.backend.domain.comment.dto.CommentLikeToggleResponseDto;
import com.team01.backend.domain.comment.dto.CommentReadResponseDto;
import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.dto.CommentResponseDto;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글", description = "댓글 관련 API")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // COMMENT-02 댓글(답글) 조회
    @Operation(summary = "댓글 조회", description = "특정 게시글의 댓글/대댓글 목록을 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentReadResponseDto>>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<CommentReadResponseDto> list = commentService.getCommentsByPostId(postId, email);
        return ResponseEntity.ok(ApiResponse.ofSuccess(list));
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글 또는 대댓글을 작성합니다.")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDto>> writeComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto reqDto,
            @AuthenticationPrincipal UserDetails userDetails){

        CommentResponseDto resDto = commentService.writeComment(
                postId, reqDto, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(resDto));
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글을 수정합니다. 본인 댓글만 수정 가능합니다.")
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponseDto resDto = commentService.updateComment(
                commentId, requestDto, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(resDto));
    }

    /** 댓글 좋아요 토글 — 한 번 호출 시 좋아요, 한 번 더 호출 시 취소 */
    @Operation(summary = "댓글 좋아요 토글", description = "댓글 좋아요를 등록/취소합니다.")
    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<ApiResponse<CommentLikeToggleResponseDto>> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentLikeToggleResponseDto dto = commentService.toggleCommentLike(commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ofSuccess(dto));
    }


    // COMMENT-04 댓글(답글) 삭제 — DELETE, 소프트 딜리트(서비스에서 isDeleted 처리)
    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 소프트 삭제합니다. 본인 댓글만 삭제 가능합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentDeleteResponseDto>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentDeleteResponseDto body = commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ofSuccess(body));
    }
}
