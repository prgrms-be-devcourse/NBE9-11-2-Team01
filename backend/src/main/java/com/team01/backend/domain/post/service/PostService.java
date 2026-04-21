package com.team01.backend.domain.post.service;

import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import com.team01.backend.domain.comment.dto.CommentReadResponseDto;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.post.dto.PostDetailResponseDto;
import com.team01.backend.domain.post.dto.PostPageResponseDto;
import com.team01.backend.domain.post.dto.PostResponseDto;
import com.team01.backend.domain.post.dto.PostSummaryDto;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentService commentService;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CategoryRepository categoryRepository;

    private static final int PAGE_SIZE = 20;

    // 페이징 공통 메서드(헬퍼 메서드)
    private Pageable toPageable(int page) {
        return PageRequest.of(page - 1, PAGE_SIZE, Sort.by("createdAt").descending());
    }

    // 조회 시 검증 로직 따로 분리
    private void validatePost(Post post) {
        if (post.isDeleted()) {
            throw new EntityNotFoundException("존재하지 않는 게시글입니다.");
        }

        Board board = post.getBoard();
        if (board == null || board.isDeleted()) {
            throw new EntityNotFoundException("존재하지 않는 게시판입니다.");
        }

        Category category = post.getCategory();
        if (category == null) {
            throw new EntityNotFoundException("존재하지 않는 카테고리입니다.");
        }
    }

    // 카테고리가 해당 게시판 소속인지 검증
    private void validateCategoryInBoard(Long categoryId, Long boardId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        // 카테고리가 요청한 게시판 소속이 아니면 예외
        if (!category.getBoardId().equals(boardId)) {
            throw new IllegalArgumentException("해당 게시판에서 사용할 수 없는 카테고리입니다.");
        }
    }

//    @Transactional
//    public Post write(User author, String title, String content) {
//        Post post = new Post(author, title, content);
//        return postRepository.save(post);
//    }

    @Transactional
    public Post write(String email, String title, String content, Long boardId, Long categoryId) {

        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 게시판, 카테고리 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시판입니다."));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다."));

        Post post = new Post(author, title, content, board, category);
        return postRepository.save(post);
    }

    public long count() {
        return postRepository.count();
    }

    // 게시판별 게시글 목록 페이징 조회 (키워드 검색, 카테고리 필터 포함)
    public PostPageResponseDto getPostsByBoardId(Long boardId, int page, String keyword, Long categoryId) {
        boardRepository.findByIdAndIsDeletedFalse(boardId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시판입니다."));

        Pageable pageable = toPageable(page);  // 교체

        Page<PostResponseDto> postPage = postRepository
                .searchByBoardId(boardId, keyword, categoryId, pageable)
                .map(PostResponseDto::new);

        return PostPageResponseDto.from(postPage);
    }

    // 게시글 상세 조회 (비로그인 사용자는 Controller에서 차단, 작성자 여부 포함)
    public PostDetailResponseDto getPostById(Long postId, String email) {
        Post post = postRepository.findWithDetailsById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        validatePost(post);

        List<CommentReadResponseDto> comments = commentService.getCommentsByPostId(postId);

        // JWT 인증된 사용자면 조회, 비로그인(토큰 없음)이면 null
        User currentUser = (email != null)
                ? userRepository.findByEmail(email).orElse(null)
                : null;

        // 작성자 본인 여부 확인 (비로그인이거나 작성자가 아니면 false)
        boolean isOwner = currentUser != null && post.getAuthor().getId().equals(currentUser.getId());

        return PostDetailResponseDto.of(post, post.getBoard(), post.getCategory(), comments, isOwner);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post modify(Long postId, String email, String title, String content, Long categoryId) {

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // api 요청자 actor 찾기
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(actor.getId())) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        // 변경하려고 하는 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        // 변경하려고 하는 카테고리가 현재 게시글의 게시판에 속하는지
        if (!category.getBoardId().equals(post.getBoard().getId())) {
            throw new IllegalArgumentException("해당 게시판에서 사용할 수 없는 카테고리입니다.");
        }

        post.update(title, content, category);

        return post;

    }

    @Transactional
    public void delete(Long postId, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다."));

        // 요청 유저 찾기
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (post.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 게시물입니다.");
        }

        if (!post.getAuthor().getId().equals(actor.getId())) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        post.delete();
    }

    @Transactional(readOnly = true)
    public PostPageResponseDto getPostsByBoardAndCategory(Long boardId, Long categoryId, int page, String keyword) {
        validateCategoryInBoard(categoryId, boardId);
        Pageable pageable = toPageable(page);

        // categoryId 고정, keyword 검색 포함하여 QueryDSL로 조회
        Page<PostResponseDto> postPage = postRepository
                .searchByBoardId(boardId, keyword, categoryId, pageable)
                .map(PostResponseDto::new);

        return PostPageResponseDto.from(postPage);
    }
}
