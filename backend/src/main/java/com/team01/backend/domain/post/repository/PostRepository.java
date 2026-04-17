package com.team01.backend.domain.post.repository;

import com.team01.backend.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByBoardIdAndIsDeletedFalse(Long boardId, Pageable pageable);

    List<Post> findByBoardIdAndIsDeletedFalse(Long boardId);

    @Query("select p from Post p " +
            "join fetch p.board " +
            "join fetch p.category " +
            "join fetch p.author " +
            "where p.board.id = :boardId and p.category.id = :categoryId")
    List<Post> findAllByBoardIdAndCategoryId(@Param("boardId") Long boardId, @Param("categoryId") Long categoryId);

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
