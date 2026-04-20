package com.team01.backend.domain.board.service;

import com.team01.backend.domain.board.dto.*;
import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import com.team01.backend.domain.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;

    // 게시판 생성, dto 형식으로 반환
    @Transactional
    public BoardCreateResponseDto createBoard(String name, String description){
        // 삭제되지 않은 게시판 중 중복이 있는지 확인
        if(boardRepository.existsByNameAndIsDeletedFalse(name)){
            throw new IllegalArgumentException("중복된 이름입니다");
        }
        Board board = new Board(name, description);
        boardRepository.save(board);
        return new BoardCreateResponseDto(board);
    }

    // 게시판 목록 조회
    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAllByIsDeletedFalse()
                .stream()
                .map(board -> BoardResponse.from(
                        board,
                        // 삭제된 게시글 제외한 게시판별 게시글 수
                        postRepository.countByBoardIdAndIsDeletedFalse(board.getId())
                ))
                .toList();
    }

    // 관리자 게시판 목록 조회 (삭제된것까지 포함해서)
    public AdminBoardListResponseDto getAllBoardsByAdmin() {
        List<AdminBoardResponseDto> exist = boardRepository.findAllByIsDeletedFalse()
                .stream()
                .map(AdminBoardResponseDto::new)
                .toList();
        List<AdminBoardResponseDto> deleted = boardRepository.findAllByIsDeletedTrue()
                .stream()
                .map(AdminBoardResponseDto::new)
                .toList();
        return new AdminBoardListResponseDto(exist, deleted);
    }

    // 게시판 수정, dto 형식으로 반환
    @Transactional
    public BoardUpdateResponseDto updateBoard(Long id, String name, String description) {
        Board board = boardRepository.findByIdAndIsDeletedFalse(id).orElseThrow(EntityNotFoundException::new);
        if(boardRepository.existsByNameAndIsDeletedFalse(name)){
            throw new IllegalArgumentException("중복된 이름입니다");
        }
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
        Board board = boardRepository.findByIdAndIsDeletedFalse(id).orElseThrow(EntityNotFoundException::new); // 없는 id, 삭제된 게시판 예외 처리
        board.setDeleted(true); // soft delete
        boardRepository.save(board);
    }

    public boolean existsById(Long id) {
        return boardRepository.existsById(id);
    }
}
