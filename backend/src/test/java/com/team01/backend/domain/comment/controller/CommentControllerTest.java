package com.team01.backend.domain.comment.controller;


import com.jayway.jsonpath.JsonPath;
import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import com.team01.backend.domain.comment.dto.CommentDeleteResponseDto;
import com.team01.backend.domain.comment.entity.Comment;
import com.team01.backend.domain.comment.repository.CommentRepository;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Post testPost2;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .password("1234")
                .build());

        Board testBoard = boardRepository.save(new Board("자유게시판", "자유롭게 글을 쓰는 곳"));
        Category testCategory = categoryRepository.save(new Category(testBoard.getId(), "일반"));

        testPost = postRepository.save(new Post(testUser, "테스트 게시글", "내용", testBoard, testCategory));
        testPost2 = postRepository.save(new Post(testUser, "테스트 게시글2", "내용2", testBoard, testCategory));
    }

    @Test
    @DisplayName("댓글 생성 - 1번 글에 생성")
    void t1() throws Exception {

        String content = "새로운 댓글";

        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "content": "%s"
                                    }
                                    """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.author").value("유저1"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("댓글 생성 - 내용이 없을 시 예외")
    void t2() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "content": ""
                                    }
                                    """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))  // 에러 코드
                .andExpect(jsonPath("$.message").exists()); // 에러 메시지 존재
    }

    @Test
    @DisplayName("댓글 작성 실패 - 없는 게시글")
    void t3() throws Exception {

        Long postId = 999L;

        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "댓글 내용"
                                }
                                """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isNotFound())               // 404
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 삭제된 게시글")
    void t4() throws Exception {
        ReflectionTestUtils.setField(testPost, "isDeleted", true);
        postRepository.saveAndFlush(testPost);

        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "댓글 내용"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isBadRequest())             // 400
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 500자 초과")
    void t5() throws Exception {

        // 501자 문자열 생성
        String longContent = "a".repeat(501);

        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "%s"
                                }
                                """.formatted(longContent))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isBadRequest())             // 400
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("대댓글 생성 - 1번 댓글에 대댓글 작성")
    void t6() throws Exception {

        // 1. 먼저 부모 댓글 생성
        String createResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "부모 댓글"
                                }
                                """)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. 부모 댓글 id 추출
        int parentId = JsonPath.read(createResponse, "$.data.id");

        // 3. 대댓글 작성
        String childContent = "대댓글이에요";

        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "%s",
                                    "parentId": %d
                                }
                                """.formatted(childContent, parentId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.content").value(childContent))
                .andExpect(jsonPath("$.data.author").value("유저1"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 대댓글에 답글 달기 불가")
    void t7() throws Exception {

        // 1. 부모 댓글 생성
        String parentResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "부모 댓글"
                                }
                                """)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        int parentId = JsonPath.read(parentResponse, "$.data.id");

        // 2. 대댓글 생성
        String childResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "대댓글",
                                    "parentId": %d
                                }
                                """.formatted(parentId))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        int childId = JsonPath.read(childResponse, "$.data.id");

        // 3. 대댓글의 대댓글 시도 → 실패해야 함
        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "대댓글의 대댓글",
                                    "parentId": %d
                                }
                                """.formatted(childId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isBadRequest())             // 400
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("답글에는 답글을 달 수 없습니다"));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 다른 게시글의 댓글에 대댓글 작성")
    void t8() throws Exception {

        String createResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "1번 게시글 댓글"
                                }
                                """)
                )
                .andReturn().getResponse().getContentAsString();

        int parentId = JsonPath.read(createResponse, "$.data.id");

        ResultActions resultActions = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost2.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "content": "잘못된 대댓글",
                                    "parentId": %d
                                }
                                """.formatted(parentId))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("잘못된 게시글의 댓글입니다."));
    }

    @Test
    @DisplayName("댓글 수정 - 1번 댓글 수정")
    void t9() throws Exception {

        ResultActions createResult = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "content": "원래 댓글"
                                    }
                                    """)
                );

        // 생성된 댓글 id 추출
        String response = createResult.andReturn()
                .getResponse().getContentAsString();
        int commentId = JsonPath.read(response, "$.data.id");

        // 수정 요청
        String updatedContent = "수정된 댓글";
        ResultActions resultActions = mvc
                .perform(
                        put("/comments/%d".formatted(commentId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "content": "%s"
                                    }
                                    """.formatted(updatedContent))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("updateComment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.content").value(updatedContent))
                .andExpect(jsonPath("$.data.author").value("유저1"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    // COMMENT-02 — 삭제된 댓글·답글 조회 표기, 삭제된 부모에는 답글 불가

    @Test
    @DisplayName("댓글 조회 - 삭제된 루트는 작성자 삭제 문구로 표기")
    void getComments_deletedRoot_showsPlaceholder() throws Exception {
        String createResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "원댓글" }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long rootId = ((Number) JsonPath.read(createResponse, "$.data.id")).longValue();
        Comment root = commentRepository.findById(rootId).orElseThrow();
        root.softDelete();
        commentRepository.saveAndFlush(root);

        mvc.perform(get("/posts/%d/comments".formatted(testPost.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value(CommentDeleteResponseDto.DELETED_CONTENT_PLACEHOLDER));
    }

    @Test
    @DisplayName("댓글 조회 - 삭제된 답글은 replies에 삭제 문구로 표시")
    void getComments_deletedReply_showsPlaceholderInReplies() throws Exception {
        String rootRes = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(""" 
                                        { "content": "루트" }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long rootId = ((Number) JsonPath.read(rootRes, "$.data.id")).longValue();

        String replyRes = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "답글", "parentId": %d }
                                        """.formatted(rootId))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long replyId = ((Number) JsonPath.read(replyRes, "$.data.id")).longValue();

        Comment reply = commentRepository.findById(replyId).orElseThrow();
        reply.softDelete();
        commentRepository.saveAndFlush(reply);

        mvc.perform(get("/posts/%d/comments".formatted(testPost.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("루트"))
                .andExpect(jsonPath("$.data[0].replies[0].content").value(CommentDeleteResponseDto.DELETED_CONTENT_PLACEHOLDER));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 삭제된 부모(루트)에 답글")
    void writeComment_replyToDeletedParent_fails() throws Exception {
        String rootRes = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "부모" }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long rootId = ((Number) JsonPath.read(rootRes, "$.data.id")).longValue();

        Comment root = commentRepository.findById(rootId).orElseThrow();
        root.softDelete();
        commentRepository.saveAndFlush(root);

        mvc.perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "불가 답글", "parentId": %d }
                                        """.formatted(rootId))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("삭제된 댓글에는 답글을 달 수 없습니다."));
    }

    // COMMENT-04 댓글 삭제

    @Test
    @DisplayName("댓글 삭제 - 본인 삭제 후 조회 시 소프트 딜리트 문구")
    void deleteComment_softDelete_thenGetShowsPlaceholder() throws Exception {
        String createResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "삭제할 댓글" }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long commentId = ((Number) JsonPath.read(createResponse, "$.data.id")).longValue();

        mvc.perform(delete("/comments/%d".formatted(commentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commentId))
                .andExpect(jsonPath("$.data.message").value(CommentDeleteResponseDto.DELETED_CONTENT_PLACEHOLDER));

        mvc.perform(get("/posts/%d/comments".formatted(testPost.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(commentId))
                .andExpect(jsonPath("$.data[0].content").value(CommentDeleteResponseDto.DELETED_CONTENT_PLACEHOLDER));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 본인이 아님")
    void deleteComment_notOwner_throws() throws Exception {
        String createResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "남의 댓글이 아님" }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long commentId = ((Number) JsonPath.read(createResponse, "$.data.id")).longValue();

        User other = userRepository.saveAndFlush(User.builder()
                .email("other@other.com")
                .nickname("다른사람")
                .password("1234")
                .build());

        assertThrows(AccessDeniedException.class,
                () -> commentService.deleteComment(commentId, other));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 이미 삭제됨")
    void deleteComment_alreadyDeleted() throws Exception {
        String createResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "두번삭제" }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long commentId = ((Number) JsonPath.read(createResponse, "$.data.id")).longValue();

        mvc.perform(delete("/comments/%d".formatted(commentId))).andExpect(status().isOk());

        mvc.perform(delete("/comments/%d".formatted(commentId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("이미 삭제된 댓글입니다."));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 삭제된 게시글의 댓글")
    void deleteComment_postDeleted_notFound() throws Exception {
        String createResponse = mvc
                .perform(
                        post("/posts/%d/comments".formatted(testPost.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "content": "글삭제후" }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long commentId = ((Number) JsonPath.read(createResponse, "$.data.id")).longValue();

        ReflectionTestUtils.setField(testPost, "isDeleted", true);
        postRepository.saveAndFlush(testPost);

        mvc.perform(delete("/comments/%d".formatted(commentId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

}
