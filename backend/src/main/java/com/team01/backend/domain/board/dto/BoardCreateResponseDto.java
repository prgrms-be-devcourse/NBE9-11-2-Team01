package com.team01.backend.domain.board.dto;

import com.team01.backend.domain.board.entity.Board;

import java.time.LocalDateTime;

public record BoardCreateResponseDto(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt
) {
    public BoardCreateResponseDto(Board board) {
        this(
            board.getId(),
            board.getName(),
            board.getDescription(),
            board.getCreatedAt()
        );
    }
}
