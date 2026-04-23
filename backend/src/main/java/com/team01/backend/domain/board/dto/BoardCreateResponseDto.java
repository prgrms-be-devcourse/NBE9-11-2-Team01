package com.team01.backend.domain.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team01.backend.domain.board.entity.Board;

import java.time.LocalDateTime;

public record BoardCreateResponseDto(
    Long id,
    String name,
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
