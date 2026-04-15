package com.team01.backend.domain.post.service;

import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.comment.dto.CommentReadResponseDto;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.post.dto.PostDetailResponseDto;
import com.team01.backend.domain.post.dto.PostResponseDto;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentService commentService;

//    @Transactional
//    public Post write(User author, String title, String content) {
//        Post post = new Post(author, title, content);
//        return postRepository.save(post);
//    }

    @Transactional
    public Post write(String title, String content) {
        Post post = new Post(title, content);
        return postRepository.save(post);
    }

    public long count() {
        return postRepository.count();
    }

    public List<PostResponseDto> getPostsByBoardId(Long boardId) {
        return postRepository.findByBoardIdAndIsDeletedFalse(boardId)
                .stream()
                .map(PostResponseDto::new)
                .toList();
    }

    public PostDetailResponseDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        if (post.isDeleted()) {
            throw new EntityNotFoundException("존재하지 않는 게시글입니다.");
        }

        Board board = post.getBoard();
        if (board == null) {
            throw new EntityNotFoundException("존재하지 않는 게시판입니다.");
        }

        Category category = post.getCategory();
        if (category == null) {
            throw new EntityNotFoundException("존재하지 않는 카테고리입니다.");
        }

        List<CommentReadResponseDto> comments = commentService.getCommentsByPostId(postId);

        return PostDetailResponseDto.of(post, board, category, comments);
    }

    public Optional<Post> findById(Long id) {return postRepository.findById(id);}

    @Transactional
    public Post modify(Long id, String title, String content) {
        Post post = postRepository.findById(id).get();
        post.update(title, content);

        return post;

    }

}
