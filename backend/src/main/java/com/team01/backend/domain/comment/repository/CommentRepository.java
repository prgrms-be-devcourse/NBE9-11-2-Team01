package com.team01.backend.domain.comment.repository;

// COMMENT-02 댓글(답글) 조회 — 아래 선언은 조회 전용(루트·답글 일괄)

import com.team01.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // COMMENT-02 댓글(답글) 조회 — 게시글별 루트 댓글
    @EntityGraph(attributePaths = "user")
    List<Comment> findByPost_IdAndParentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);

    // COMMENT-02 댓글(답글) 조회 — 루트 id 목록에 대한 답글 일괄 조회(N+1 방지)
    @EntityGraph(attributePaths = "user")
    List<Comment> findByParent_IdInAndIsDeletedFalseOrderByCreatedAtAsc(List<Long> parentIds);
}
