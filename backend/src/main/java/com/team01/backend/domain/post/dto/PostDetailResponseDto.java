package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;

// TODO : commit 후에 주석 해제
public record PostDetailResponseDto(
        Long id,
        BoardInfo board,
        //CategoryInfo category,
        String title,
        String content,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    // 중첩 record: Board에서 필요한 것만
    public record BoardInfo(Long id, String name) {}

    // 중첩 record: Category에서 필요한 것만
    //public record CategoryInfo(Long id, String name) {}

    // TODO : category 파라미터로 넘겨줘야 함
    public static PostDetailResponseDto of(Post post, Board board) {
        return new PostDetailResponseDto(
                post.getId(),
                new BoardInfo(board.getId(), board.getName()),
                //new CategoryInfo(category.getId(), category.getName()),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
