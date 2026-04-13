package com.team01.backend.domain.comment.controller;


import com.jayway.jsonpath.JsonPath;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

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

        String title = "테스트 게시글";
        String content ="내용";

        Post post = new Post(title, content);

        testPost = postRepository.save(post);
    }

    @Test
    @DisplayName("댓글 생성 - 1번 글에 생성")
    void t1() throws Exception {

        int targetPostId = 1;
        String content = "새로운 댓글";

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(targetPostId))
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
                .andExpect(jsonPath("$.data.id").value(1))           // commentDto 제거
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.author").value("테스터"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("댓글 생성 - 내용이 없을 시 예외")
    void t2() throws Exception {

        int targetPostId = 1;
        String content = "";

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(targetPostId))
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))  // 에러 코드
                .andExpect(jsonPath("$.message").exists()); // 에러 메시지 존재
    }

    @Test
    @DisplayName("대댓글 생성 - 1번 댓글에 대댓글 작성")
    void t3() throws Exception {

        // 1. 먼저 부모 댓글 생성
        String createResponse = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(1))
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
                        post("/api/v1/posts/%d/comments".formatted(1))
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
                .andExpect(jsonPath("$.data.author").value("테스터"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 대댓글에 답글 달기 불가")
    void t4() throws Exception {

        // 1. 부모 댓글 생성
        String parentResponse = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(1))
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
                        post("/api/v1/posts/%d/comments".formatted(1))
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
                        post("/api/v1/posts/%d/comments".formatted(1))
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
    @DisplayName("댓글 수정 - 1번 댓글 수정")
    void t5() throws Exception {

        // 먼저 댓글 생성
        int targetPostId = 1;
        ResultActions createResult = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(targetPostId))
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
                        put("/api/v1/comments/%d".formatted(commentId))
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
                .andExpect(jsonPath("$.data.author").value("테스터"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

}
