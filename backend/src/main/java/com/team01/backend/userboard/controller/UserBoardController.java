package com.team01.backend.userboard.controller;

import com.team01.backend.userboard.dto.UserBoardResponse;
import com.team01.backend.userboard.service.UserBoardService;
import com.team01.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class UserBoardController {
    private final UserBoardService userBoardService;

    // 게시판 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserBoardResponse>>> getAllBoards() {
        List<UserBoardResponse> boards = userBoardService.getAllBoards();
        return ResponseEntity.ok(ApiResponse.ofSuccess(boards));
    }
}
