package com.team01.backend.domain.comment.repository;

import com.team01.backend.domain.comment.entity.Comment;
import com.team01.backend.domain.comment.entity.CommentLike;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    /**
     * 좋아요 토글 등 동시 요청 시 {@code likeCount}·{@code CommentLike} 정합성을 위해 댓글 행을 직렬화합니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.id = :id")
    Optional<Comment> findCommentByIdAndPostForUpdate(@Param("id") Long id);

    Optional<CommentLike> findByComment_IdAndUser_Id(Long commentId, Long userId);

    /** 삭제된 행 수 — 0이면 멱등(이미 없음), 동시성 시 이중 감소 방지에 사용 */
    int deleteByComment_IdAndUser_Id(Long commentId, Long userId);

    long countByComment_Id(Long commentId);

    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds")
    List<Long> findLikedCommentIdsByUserId(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);
}
