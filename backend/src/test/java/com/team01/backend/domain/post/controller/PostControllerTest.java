package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.post.service.PostService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("게시판별 글 목록 조회 - 성공")
    void t1() throws Exception {
        // given: BaseInitData에서 생성된 데이터 사용

        // when
        ResultActions resultActions = mvc
                .perform(get("/boards/1/posts"))
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getPostsByBoardId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("게시판별 글 목록 조회 - 빈 배열")
    void t2() throws Exception {
        // when
        ResultActions resultActions = mvc
                .perform(get("/boards/999/posts"))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("글 작성")
    void t3() throws Exception {
        String title = "제목입니다.";
        String content = "내용입니다.";

        ResultActions resultActions = mvc
                .perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        """.formatted(title, content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postDto.id").exists())
                .andExpect(jsonPath("$.data.postDto.title").value(title))
                .andExpect(jsonPath("$.data.postDto.content").value(content))
                .andExpect(jsonPath("$.data.postDto.createdAt").exists())
                .andExpect(jsonPath("$.data.postDto.modifiedAt").exists());
    }

    @Test
    @DisplayName("글 작성 실패 - 제목이 입력되지 않은 경우")
    void t4() throws Exception {

        String title = "";
        String content = "내용입니다.";


        ResultActions resultActions = mvc
                .perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "title": "%s",
                                        "content": "%s"
                                    }
                                    """.formatted(title, content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("글 작성, 내용이 입력되지 않은 경우")
    void t5() throws Exception {
        String title = "제목입니다.";
        String content = "";

        ResultActions resultActions = mvc
                .perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        """.formatted(title, content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("글 작성 실패 - JSON 양식이 잘못된 경우")
    void t6() throws Exception {

        String title = "제목입니다.";
        String content = "내용입니다.";

        ResultActions resultActions = mvc
                .perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "title": "%s"   // , 누락
                                        "content": "%s"
                                    }
                                    """.formatted(title, content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_JSON"))
                .andExpect(jsonPath("$.message").value("잘못된 형식의 JSON 데이터입니다."));
    }

    @Test
    @DisplayName("글 수정")
    void t7() throws Exception {

        Long targetId = 1L;

        String title = "제목 수정";
        String content = "내용 수정";

        ResultActions resultActions = mvc
                .perform(
                        put("/posts/%d".formatted(targetId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                                {
                                                    "title" : "%s",
                                                    "content" : "%s"
                                                }
                                                """.formatted(title, content))
                )
                .andDo(print());

        // 필수 검증
        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true)) // success 필드 확인
                .andExpect(jsonPath("$.data.postDto.title").value(title)) // 수정된 데이터가 바로 오는지 확인
                .andExpect(jsonPath("$.data.postDto.content").value(content));

        Post post = postRepository.findById(targetId)
                .orElseThrow(() -> new AssertionError("게시물이 DB에 존재하지 않습니다."));

        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
    }
}
