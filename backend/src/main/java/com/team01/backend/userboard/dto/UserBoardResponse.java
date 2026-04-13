package com.team01.backend.userboard.dto;

import com.team01.backend.userboard.entity.UserBoard;

import java.time.LocalDateTime;

public record UserBoardResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static UserBoardResponse from(UserBoard board) {
        return new UserBoardResponse(
                board.getId(),
                board.getName(),
                board.getDescription(),
                board.getCreatedAt(),
                board.getModifiedAt()
        );
    }
}
