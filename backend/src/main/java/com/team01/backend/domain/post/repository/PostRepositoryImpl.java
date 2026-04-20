package com.team01.backend.domain.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.entity.QPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

// PostRepositoryCustom 구현체 - QueryDSL로 동적 검색 쿼리 처리

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;

    @Override
    public Page<Post> searchByBoardId(Long boardId, String keyword, Long categoryId, Pageable pageable) {
        List<Post> posts = queryFactory
                .selectFrom(post)
                .join(post.author).fetchJoin()
                .join(post.category).fetchJoin()
                .where(
                        post.board.id.eq(boardId),
                        post.isDeleted.eq(false),
                        containsKeyword(keyword),
                        eqCategoryId(categoryId)
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.board.id.eq(boardId),
                        post.isDeleted.eq(false),
                        containsKeyword(keyword),
                        eqCategoryId(categoryId)
                )
                .fetchOne();

        return new PageImpl<>(posts, pageable, total != null ? total : 0);
    }

    private BooleanExpression eqCategoryId(Long categoryId) {
        if (categoryId == null) return null;
        return post.category.id.eq(categoryId);
    }

    // 검색어 유효성 검사 및 XSS 방지 처리 후 제목 검색 조건 반환 (null이면 전체 조회, 공백이면 빈 리스트 반환)
    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null) return null;
        if (keyword.isBlank()) return Expressions.FALSE;
        String sanitized = keyword.trim()
                .replace("<", "")
                .replace(">", "")
                .replace("&", "");
        return post.title.containsIgnoreCase(sanitized);
    }
}
