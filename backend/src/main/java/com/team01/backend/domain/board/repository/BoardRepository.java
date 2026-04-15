package com.team01.backend.domain.board.repository;

import com.team01.backend.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board,Long> {
    Optional<Board> findByIdAndIsDeletedFalse(Long id);
}
