package com.team01.backend.domain.board.service;

import com.team01.backend.domain.board.dto.BoardCreateResponseDto;
import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    // 게시판 생성, dto 형식으로 반환
    public BoardCreateResponseDto createBoard(String name, String description){
        Board board = new Board(name, description);
        boardRepository.save(board);
        return new BoardCreateResponseDto(board);
    }
}
