package com.team01.backend.userboard.service;

import com.team01.backend.userboard.dto.UserBoardResponse;
import com.team01.backend.userboard.repository.UserBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBoardService {

    private final UserBoardRepository userBoardRepository;

    // 게시판 목록 조회
    public List<UserBoardResponse> getAllBoards() {
        return userBoardRepository.findAll()
                .stream()
                .map(UserBoardResponse::from)
                .toList();
    }
}
