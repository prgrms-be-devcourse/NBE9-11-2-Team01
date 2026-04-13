package com.team01.backend.domain.comment.service;

import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.dto.CommentResponseDto;
import com.team01.backend.domain.comment.entity.Comment;
import com.team01.backend.domain.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponseDto writeComment(Long postId, CommentRequestDto reqDto, User loginUser){

        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 업습니다."));

        // 삭제된 게시글 확인 -> code:400
        if(post.isDeleted()){
            throw new IllegalArgumentException("삭제된 게시글에는 댓글을 달 수 없습니다.");
        }

        // 대댓글인지 확인 부모 댓글 없을 시 예외처리 -> code : 404
        Comment parent = null;
        if(reqDto.parentId() != null){
            parent = commentRepository.findById(reqDto.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));
        }

        Comment comment = new Comment(post, loginUser, reqDto.content(), parent);

        commentRepository.save(comment);

        return CommentResponseDto.from(comment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto reqDto, User loginUser){

        //댓글 존재 확인 -> code:404
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if(!comment.getUser().getId().equals(loginUser.getId())){
            throw new IllegalArgumentException("본인 댓글만 수정할 수 있습니다.");
        }

        comment.update(reqDto.content());

        return CommentResponseDto.from(comment);
    }
}
