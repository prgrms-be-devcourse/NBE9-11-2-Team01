package com.team01.backend.domain.comment.service;

import com.team01.backend.domain.comment.dto.CommentReadResponseDto;
import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.dto.CommentResponseDto;
import com.team01.backend.domain.comment.entity.Comment;
import com.team01.backend.domain.comment.repository.CommentRepository;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 수 조회
    public long count() {
        return commentRepository.count();
    }

    // 초기 데이터용
    @Transactional
    public void writeInitComment(Long postId, User tempUser, String content,  Long parentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없어요"));
        if (post.isDeleted()) {
            throw new EntityNotFoundException("게시글을 찾을 수 없어요");
        }

        // parentId 있으면 부모 댓글 찾기, 없으면 null
        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));
        }

        commentRepository.save(new Comment(post, tempUser, content,  parent));
    }

    //-----------------------------------------------------------------------------------------------------------------

    // COMMENT-02 댓글(답글) 조회
    @Transactional(readOnly = true)
    public List<CommentReadResponseDto> getCommentsByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        if (post.isDeleted()) {
            throw new EntityNotFoundException("게시글을 찾을 수 없습니다.");
        }

        List<Comment> roots = commentRepository
                .findByPost_IdAndParentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(postId);
        if (roots.isEmpty()) {
            return List.of();
        }

        List<Long> rootIds = roots.stream().map(Comment::getId).toList();
        List<Comment> allReplies =
                commentRepository.findByParent_IdInAndIsDeletedFalseOrderByCreatedAtAsc(rootIds);
        Map<Long, List<Comment>> repliesByParentId =
                allReplies.stream().collect(Collectors.groupingBy(c -> c.getParent().getId()));
        repliesByParentId.values().forEach(list -> list.sort(Comparator.comparing(Comment::getCreatedAt)));

        return roots.stream()
                .map(root -> CommentReadResponseDto.from(
                        root, repliesByParentId.getOrDefault(root.getId(), List.of())))
                .toList();
    }

    @Transactional
    public CommentResponseDto writeComment(Long postId, CommentRequestDto reqDto, User loginUser){

        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // 삭제된 게시글 확인 -> code:400
        if(post.isDeleted()){
            throw new IllegalArgumentException("삭제된 게시글에는 댓글을 달 수 없습니다.");
        }

        // 대댓글인지 확인 부모 댓글 없을 시 예외처리 -> code : 404
        Comment parent = null;
        if(reqDto.parentId() != null){
            parent = commentRepository.findById(reqDto.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));

            if (!parent.getPost().getId().equals(postId)) {
                throw new IllegalArgumentException("잘못된 게시글의 댓글입니다.");
            }

            // 답글의 대댓글 방지
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("답글에는 답글을 달 수 없습니다.");
            }
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

        //삭제된 댓글을 수정할 수 없음.
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        // userId를 비교하여 본인 인증 -> 추후 시큐리티 사용시 변경 필요
        if(!comment.getUser().getId().equals(loginUser.getId())){
            throw new IllegalArgumentException("본인 댓글만 수정할 수 있습니다.");
        }

        comment.update(reqDto.content());

        return CommentResponseDto.from(comment);
    }
}
