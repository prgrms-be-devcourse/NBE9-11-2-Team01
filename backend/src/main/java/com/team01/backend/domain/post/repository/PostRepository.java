package com.team01.backend.domain.post.repository;

import com.team01.backend.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findByBoardIdAndIsDeletedFalse(Long boardId, Pageable pageable);
}
