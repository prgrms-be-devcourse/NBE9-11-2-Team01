package com.team01.backend.domain.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team01.backend.domain.board.entity.Board;

import java.time.LocalDateTime;

public record BoardUpdateResponseDto (
        Long id,
        String name,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
