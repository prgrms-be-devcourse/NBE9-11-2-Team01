package com.team01.backend.domain.post.repository;

import com.team01.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 게시판별 글 전체 조회 (삭제되지 않은 글만)
    List<Post> findByBoardIdAndIsDeletedFalse(Long boardId);

}
