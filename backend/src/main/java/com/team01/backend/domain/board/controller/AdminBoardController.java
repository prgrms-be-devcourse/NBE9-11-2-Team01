package com.team01.backend.domain.board.controller;

import com.team01.backend.domain.board.dto.BoardCreateResponseDto;
import com.team01.backend.domain.board.dto.BoardUpdateResponseDto;
import com.team01.backend.domain.board.service.BoardService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/board")
public class AdminBoardController {

    private final BoardService boardService;

    // 게시판 생성 request
    record BoardCreateReqBody(
            @NotNull
            @Size(min = 2)
            String name,

            @Size(min = 5)
            @NotNull String description
    ){};

    // 게시판 생성, reqBody에서 문제가 있다면(null, size) globalExceptionHandler에서 처리됨
    @PostMapping
    ResponseEntity<ApiResponse<BoardCreateResponseDto>> createBoard(
            @RequestBody @Valid BoardCreateReqBody reqBody
    ){
        BoardCreateResponseDto boardCreateResponseDto = boardService.createBoard(reqBody.name(), reqBody.description());
        return ResponseEntity.ok(ApiResponse.ofSuccess(
                boardCreateResponseDto
        ));
    }

    // 게시판 수정 request
    record BoardUpdateReqBody(
            @NotNull
            @Size(min = 2)
            String name,

            @Size(min = 5)
            @NotNull String description
    ){};

    // 게시판 수정, reqBody에서 문제가 있다면(null, size) globalExceptionHandler에서 처리됨
    @PostMapping("/{boardId}")
    ResponseEntity<ApiResponse<BoardUpdateResponseDto>> updateBoard(
            @PathVariable Long boardId,
            @RequestBody @Valid BoardUpdateReqBody reqBody
    ){
        BoardUpdateResponseDto boardUpdateResponseDto = boardService.updateBoard(boardId, reqBody.name(), reqBody.description());
        return ResponseEntity.ok(ApiResponse.ofSuccess(
                boardUpdateResponseDto
        ));
    }


}
