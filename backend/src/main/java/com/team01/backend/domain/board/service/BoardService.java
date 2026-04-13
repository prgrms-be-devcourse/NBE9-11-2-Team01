package com.team01.backend.domain.board.service;

import com.team01.backend.domain.board.dto.BoardCreateResponseDto;
import com.team01.backend.domain.board.dto.BoardUpdateResponseDto;
import com.team01.backend.domain.board.dto.BoardResponse;
import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    // 게시판 생성, dto 형식으로 반환
    @Transactional
    public BoardCreateResponseDto createBoard(String name, String description){
        Board board = new Board(name, description);
        boardRepository.save(board);
        return new BoardCreateResponseDto(board);
    }

    // 게시판 목록 조회
    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAll()
                .stream()
                .map(BoardResponse::from)
                .toList();
    }

    // 게시판 수정, dto 형식으로 반환
    @Transactional
    public BoardUpdateResponseDto updateBoard(Long id, String name, String description) {
        Board board = boardRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        board.update(name, description);
        boardRepository.save(board);
        return new BoardUpdateResponseDto(board);
    }

    // board 수 반환
    public long count() {
        return boardRepository.count();
    }

    // 게시판 삭제
    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(EntityNotFoundException::new); // 없는 id 예외 처리
        boardRepository.delete(board);
    }
}
