package com.team01.backend.domain.comment.dto;

// COMMENT-02 댓글(답글) 조회 — 응답 전용(작성·수정용 CommentResponseDto와 분리)

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team01.backend.domain.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

// 게시글 상세 조회 테스트 결과 답글 응답에 replies 빈 배열이 불필요하게 포함되는 문제가 있어서 추가
// 답글의 replies는 항상 빈 배열이라 응답에 불필요 — NON_EMPTY로 빈 배열 필드 제외
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(CommentReadResponseDto::fromReply)
                .toList();
        return new CommentReadResponseDto(
                root.getId(),
                CommentDeleteResponseDto.contentForRead(root),
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
                CommentDeleteResponseDto.contentForRead(reply),
                reply.getUser().getNickname(),
                reply.getLikeCount(),
                reply.getCreatedAt(),
                reply.getModifiedAt(),
                List.of()
        );
    }
}
