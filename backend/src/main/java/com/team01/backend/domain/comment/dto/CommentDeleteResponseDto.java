package com.team01.backend.domain.comment.dto;

import com.team01.backend.domain.comment.entity.Comment;

// COMMENT-04 댓글 삭제 — 응답·소프트 삭제 마스킹 문구(조회 COMMENT-02와 동일 규칙)

/**
 * @param id      삭제된 댓글 id
 * @param message 삭제 API 성공 시 안내 문구(조회 시 {@link #contentForRead(Comment)}와 동일)
 */
public record CommentDeleteResponseDto(Long id, String message) {

    /** 소프트 삭제된 댓글·답글의 조회/API 표기 문구(팀 규칙: {@code isDeleted}) */
    public static final String DELETED_CONTENT_PLACEHOLDER = "작성자에 의해 삭제된 댓글입니다.";

    /** COMMENT-02 조회용 — 삭제된 엔티티는 원문 대신 플레이스홀더 */
    public static String contentForRead(Comment comment) {
        return comment.isDeleted() ? DELETED_CONTENT_PLACEHOLDER : comment.getContent();
    }

    public static CommentDeleteResponseDto of(Long commentId) {
        return new CommentDeleteResponseDto(commentId, DELETED_CONTENT_PLACEHOLDER);
    }
}
