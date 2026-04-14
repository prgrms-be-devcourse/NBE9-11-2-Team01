package com.team01.backend.domain.comment.controller;


import com.jayway.jsonpath.JsonPath;
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
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;


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
                .andExpect(jsonPath("$.data.author").value("유저"))
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
        // 게시글 소프트 딜리트
        //testPost.delete();
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
                .andExpect(jsonPath("$.data.author").value("유저"))
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
    @DisplayName("댓글 수정 - 1번 댓글 수정")
    void t8() throws Exception {

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
                .andExpect(jsonPath("$.data.author").value("유저"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

}
