package com.team01.backend.domain.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team01.backend.domain.board.entity.Board;

import java.time.LocalDateTime;

public record BoardResponse(
        Long id,
        String boardName,
        String description,
        long postCount,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")// 게시판별 게시글 수 (삭제된 게시글 제외)
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
