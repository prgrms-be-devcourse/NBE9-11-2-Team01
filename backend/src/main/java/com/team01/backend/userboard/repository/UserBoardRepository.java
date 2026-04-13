package com.team01.backend.userboard.repository;

import com.team01.backend.userboard.entity.UserBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBoardRepository extends JpaRepository<UserBoard, Long> {
}
