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
    @DisplayName("댓글 수정 - 1번 댓글 수정")
    void t2() throws Exception {

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
