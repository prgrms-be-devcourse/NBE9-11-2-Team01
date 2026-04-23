package com.team01.backend.domain.board.controller;

import com.team01.backend.domain.board.dto.AdminBoardListResponseDto;
import com.team01.backend.domain.board.dto.BoardCreateResponseDto;
import com.team01.backend.domain.board.dto.BoardUpdateResponseDto;
import com.team01.backend.domain.board.service.BoardService;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 게시판 관리", description = "관리자 게시판 관리 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/boards")
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
    @Operation(summary = "게시판 생성", description = "게시판을 생성합니다. 관리자 권한에서만 가능합니다.")
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
    @Operation(summary = "게시판 수정", description = "게시판을 수정합니다. 관리자 권한에서만 가능합니다.")
    @PutMapping("/{boardId}")
    ResponseEntity<ApiResponse<BoardUpdateResponseDto>> updateBoard(
            @PathVariable Long boardId,
            @RequestBody @Valid BoardUpdateReqBody reqBody
    ){
        BoardUpdateResponseDto boardUpdateResponseDto = boardService.updateBoard(boardId, reqBody.name(), reqBody.description());
        return ResponseEntity.ok(ApiResponse.ofSuccess(
                boardUpdateResponseDto
        ));
    }

    // 게시판 삭제, id만 받아서 삭제
    @Operation(summary = "게시판 삭제", description = "게시판을 Soft Delete 처리합니다. 관리자 권한에서만 가능합니다.")
    @DeleteMapping("/{boardId}")
    ResponseEntity<ApiResponse> deleteBoard(
            @PathVariable Long boardId
    ){
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccessWithoutBody());
    }

    // 게시판 다건 조회
    @Operation(summary = "게시판 조회", description = "삭제된 게시판과 유지중인 게시판을 각각의 리스트로 담아 조회합니다. 관리자 권한에서만 가능합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminBoardListResponseDto>> getBoards() {
        AdminBoardListResponseDto boards = boardService.getAllBoardsByAdmin();
        return ResponseEntity.ok(ApiResponse.ofSuccess(boards));
    }
}
