package com.team01.backend.domain.comment.repository;

// COMMENT-02 댓글(답글) 조회 — 아래 선언은 조회 전용(루트·답글 일괄)

import com.team01.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // COMMENT-04 삭제 — 댓글·게시글·작성자 동시 로딩
    @Query("SELECT DISTINCT c FROM Comment c JOIN FETCH c.post JOIN FETCH c.user WHERE c.id = :id")
    Optional<Comment> findByIdWithPost(@Param("id") Long id);

    // COMMENT-02 댓글(답글) 조회 — 게시글별 루트(삭제 포함, 응답에서 문구 마스킹)
    @EntityGraph(attributePaths = "user")
    List<Comment> findByPost_IdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    // COMMENT-02 댓글(답글) 조회 — 루트 id 목록에 대한 답글 일괄(삭제 포함, 응답에서 문구 마스킹)
    @EntityGraph(attributePaths = "user")
    List<Comment> findByParent_IdInOrderByCreatedAtAsc(List<Long> parentIds);
}
