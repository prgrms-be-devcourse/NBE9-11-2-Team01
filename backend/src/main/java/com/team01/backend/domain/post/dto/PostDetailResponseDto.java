package com.team01.backend.domain.post.dto;

import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.comment.dto.CommentReadResponseDto;
import com.team01.backend.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponseDto(
        Long id,
        Long boardId,
        String boardName,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        String author,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<CommentReadResponseDto> comments
        //boolean isOwner
) {
    // TODO: 인증/인가 구현하면 boolean isOwner 추가
    public static PostDetailResponseDto of(Post post, Board board, Category category,
                                           List<CommentReadResponseDto> comments) {
        return new PostDetailResponseDto(
                post.getId(),
                board.getId(),
                board.getName(),
                category.getId(),
                category.getName(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getNickname(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                comments
                //isOwner
        );
    }
}
