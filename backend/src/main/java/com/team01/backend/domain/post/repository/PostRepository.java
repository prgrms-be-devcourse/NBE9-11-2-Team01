package com.team01.backend.domain.post.repository;

import com.team01.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author", "category"})
    List<Post> findByBoardIdAndIsDeletedFalse(Long boardId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void increaseLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.liked > 0")
    void decreaseLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = :likeCount WHERE p.id = :id")
    void updateLikeCount(@Param("id") Long id, @Param("likeCount") int likeCount);
}
