package com.team01.backend.domain.board.dto;

import com.team01.backend.domain.board.entity.Board;

import java.time.LocalDateTime;

public record BoardResponse(
        Long id,
        String boardName,
        String description,
        long postCount,      // 게시판별 게시글 수 (삭제된 게시글 제외)
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static BoardResponse from(Board board, long postCount) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getDescription(),
                postCount,
                board.getCreatedAt(),
                board.getModifiedAt()
        );
    }
}
