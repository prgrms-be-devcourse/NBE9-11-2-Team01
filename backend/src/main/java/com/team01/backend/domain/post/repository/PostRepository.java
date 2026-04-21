package com.team01.backend.domain.post.repository;

import com.team01.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    // 페이징 미적용, findAllByBoardIdAndCategoryIdAndIsDeletedFalse 로 대체
    @Query("select p from Post p " +
            "join fetch p.board " +
            "join fetch p.category " +
            "join fetch p.author " +
            "where p.board.id = :boardId and p.category.id = :categoryId")
    List<Post> findAllByBoardIdAndCategoryId(@Param("boardId") Long boardId, @Param("categoryId") Long categoryId);

    // 전체 게시판별 게시글 수 한 번에 조회 - getAllBoards N+1 해결
    @Query("SELECT p.board.id, COUNT(p) FROM Post p WHERE p.isDeleted = false GROUP BY p.board.id")
    List<Object[]> countByBoardGrouped();

    // 게시글 상세 조회 - board, category, author 한 번에 조회 (N+1 방지)
    @EntityGraph(attributePaths = {"board", "category", "author"})
    Optional<Post> findWithDetailsById(Long id);
    // 게시판별 게시글 수 조회 (삭제된 게시글 제외)
    // BoardService.getAllBoards에서 게시판 목록 조회 시 게시판별 게시글 수 표시에 사용
    long countByBoardIdAndIsDeletedFalse(Long boardId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void increaseLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decreaseLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = :likeCount WHERE p.id = :id")
    void updateLikeCount(@Param("id") Long id, @Param("likeCount") int likeCount);
}
