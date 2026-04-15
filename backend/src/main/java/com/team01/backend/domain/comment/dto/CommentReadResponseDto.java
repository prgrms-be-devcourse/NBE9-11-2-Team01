package com.team01.backend.domain.comment.dto;

// COMMENT-02 댓글(답글) 조회 — 응답 전용 DTO(작성·수정용 CommentResponseDto와 분리)

import com.team01.backend.domain.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record CommentReadResponseDto(
        Long id,
        String content,
        String author,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<CommentReadResponseDto> replies
) {
    // COMMENT-02 댓글(답글) 조회 — 루트 + 답글 엔티티 목록 → 트리 DTO
    public static CommentReadResponseDto from(Comment root, List<Comment> replyEntities) {
        List<CommentReadResponseDto> replyDtos = replyEntities.stream()
                .filter(c -> !c.isDeleted())
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(CommentReadResponseDto::fromReply)
                .toList();
        return new CommentReadResponseDto(
                root.getId(),
                root.getContent(),
                root.getUser().getNickname(),
                root.getLikeCount(),
                root.getCreatedAt(),
                root.getModifiedAt(),
                replyDtos
        );
    }

    // COMMENT-02 댓글(답글) 조회 — 답글 1건(하위 replies 없음)
    private static CommentReadResponseDto fromReply(Comment reply) {
        return new CommentReadResponseDto(
                reply.getId(),
                reply.getContent(),
                reply.getUser().getNickname(),
                reply.getLikeCount(),
                reply.getCreatedAt(),
                reply.getModifiedAt(),
                List.of()
        );
    }
}
