package com.team01.backend.domain.board.repository;

import com.team01.backend.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board,Long> {
    Optional<Board> findByIdAndIsDeletedFalse(Long id);
    List<Board> findAllByIsDeletedFalse();
    List<Board> findAllByIsDeletedTrue();
    boolean existsByNameAndIsDeletedFalse(String name);
}
