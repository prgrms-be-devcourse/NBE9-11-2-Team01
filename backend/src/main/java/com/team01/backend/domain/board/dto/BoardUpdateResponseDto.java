package com.team01.backend.domain.board.dto;

import com.team01.backend.domain.board.entity.Board;

import java.time.LocalDateTime;

public record BoardUpdateResponseDto (
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public BoardUpdateResponseDto(Board board) {
        this(
                board.getId(),
                board.getName(),
                board.getDescription(),
                board.getCreatedAt(),
                board.getModifiedAt()
        );
    }
}
