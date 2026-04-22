package com.team01.backend.domain.category.repository;

import com.team01.backend.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByBoardIdAndName(Long boardId, String name);
    List<Category> findByBoardId(Long boardId);
}
