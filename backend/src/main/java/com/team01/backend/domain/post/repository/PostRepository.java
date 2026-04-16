package com.team01.backend.domain.post.repository;

import com.team01.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author", "category"})
    List<Post> findByBoardIdAndIsDeletedFalse(Long boardId);
}
