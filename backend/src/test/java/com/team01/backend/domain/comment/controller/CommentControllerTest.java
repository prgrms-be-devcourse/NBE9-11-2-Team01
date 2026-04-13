package com.team01.backend.domain.comment.controller;


import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.dto.CommentResponseDto;
import com.team01.backend.domain.comment.repository.CommentRepository;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CommentControllerTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .password("1234")
                .build());

        testPost = postRepository.save(Post.builder()
                .title("테스트 게시글")
                .content("내용")
                .user(testUser)
                .build());
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void 댓글_작성_성공() {
        // given (준비)
        CommentRequestDto request = new CommentRequestDto("테스트 댓글이에요", null);

        // when (실행)
        CommentResponseDto response = commentService.writeComment(
                testPost.getId(), request, testUser.getId());

        // then (검증)
        assertThat(response.content()).isEqualTo("테스트 댓글이에요");
        assertThat(response.author()).isEqualTo("테스터");
    }

    // ✅ 정상 — 대댓글 작성
    @Test
    @DisplayName("대댓글 작성 성공")
    void 대댓글_작성_성공() {
        // given
        CommentRequestDto parentRequest = new CommentRequestDto("부모 댓글", null);
        CommentResponseDto parentComment = commentService.createComment(
                testPost.getId(), parentRequest, testUser.getId());

        CommentRequestDto childRequest = new CommentRequestDto(
                "대댓글이에요", parentComment.id());  // parentId 넣기

        // when
        CommentResponseDto response = commentService.createComment(
                testPost.getId(), childRequest, testUser.getId());

        // then
        assertThat(response.content()).isEqualTo("대댓글이에요");
    }

    // ✅ 정상 — 댓글 수정
    @Test
    @DisplayName("댓글 수정 성공")
    void 댓글_수정_성공() {
        // given
        CommentRequestDto createRequest = new CommentRequestDto("원래 댓글", null);
        CommentResponseDto created = commentService.createComment(
                testPost.getId(), createRequest, testUser.getId());

        CommentRequestDto updateRequest = new CommentRequestDto("수정된 댓글", null);

        // when
        CommentResponseDto response = commentService.updateComment(
                created.id(), updateRequest, testUser.getId());

        // then
        assertThat(response.content()).isEqualTo("수정된 댓글");
    }

    // ❌ 예외 — 없는 게시글에 댓글 작성
    @Test
    @DisplayName("없는 게시글에 댓글 작성 시 예외 발생")
    void 없는_게시글_댓글_작성_실패() {
        // given
        CommentRequestDto request = new CommentRequestDto("댓글", null);
        Long 없는게시글Id = 999L;

        // when & then
        assertThatThrownBy(() ->
                commentService.createComment(없는게시글Id, request, testUser.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ❌ 예외 — 남의 댓글 수정
    @Test
    @DisplayName("남의 댓글 수정 시 예외 발생")
    void 남의_댓글_수정_실패() {
        // given — 다른 유저 만들기
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .nickname("다른유저")
                .password("1234")
                .build());

        CommentRequestDto createRequest = new CommentRequestDto("내 댓글", null);
        CommentResponseDto created = commentService.createComment(
                testPost.getId(), createRequest, testUser.getId());

        CommentRequestDto updateRequest = new CommentRequestDto("수정 시도", null);

        // when & then — 다른 유저가 수정 시도
        assertThatThrownBy(() ->
                commentService.updateComment(
                        created.id(), updateRequest, otherUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인 댓글만 수정할 수 있어요");
    }

    // ❌ 예외 — 없는 댓글 수정
    @Test
    @DisplayName("없는 댓글 수정 시 예외 발생")
    void 없는_댓글_수정_실패() {
        // given
        CommentRequestDto request = new CommentRequestDto("수정 내용", null);
        Long 없는댓글Id = 999L;

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(없는댓글Id, request, testUser.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
