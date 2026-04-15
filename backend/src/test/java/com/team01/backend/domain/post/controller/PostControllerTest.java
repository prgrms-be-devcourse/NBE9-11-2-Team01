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
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].author").exists())
                .andExpect(jsonPath("$.data[0].categoryId").exists())
                .andExpect(jsonPath("$.data[0].categoryName").exists());
    }

    @Test
    @DisplayName("게시판별 글 목록 조회 - 존재하지 않는 게시판")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/boards/999/posts"))
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("글 작성")
    void t3() throws Exception {
        String title = "제목입니다.";
        String content = "내용입니다.";
        Long boardId = 1L;
        Long categoryId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "%s",
                                            "content": "%s",
                                            "boardId" : %d,
                                            "categoryId" : %d
                                        }
                                        """.formatted(title, content, boardId, categoryId))
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
        Long boardId = 1L;
        Long categoryId = 1L;


        ResultActions resultActions = mvc
                .perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "title": "%s",
                                        "content": "%s",
                                        "boardId": %d,
                                        "categoryId": %d
                                    }
                                    """.formatted(title, content, boardId, categoryId))
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
        Long boardId = 1L;
        Long categoryId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                            "title": "%s",
                                            "content": "%s",
                                            "boardId": %d,
                                            "categoryId": %d
                                        }
                                        """.formatted(title, content, boardId, categoryId))
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

    @Test
    @DisplayName("게시글 상세 조회 - 성공")
    void t8() throws Exception {
        // given: BaseInitData에서 생성된 게시글 사용 (id=1)

        // when
        ResultActions resultActions = mvc
                .perform(get("/posts/1"))
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getPostById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.author").exists())
                .andExpect(jsonPath("$.data.comments").isArray())
                .andExpect(jsonPath("$.data.likeCount").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.modifiedAt").exists());
    }

    @Test
    @DisplayName("게시글 상세 조회 - 존재하지 않는 게시글")
    void t9() throws Exception {
        // when
        ResultActions resultActions = mvc
                .perform(get("/posts/999"))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }


    @Test
    @DisplayName("글 삭제 성공")
    void t10() throws Exception {
//        Post post = postRepository.findById(1L).get();
//        Long targetId = post.getId();

        Post post = postService.write("테스트 제목", "테스트 내용", 1L, 1L);
        Long targetId = post.getId();

        ResultActions resultActions = mvc
                .perform(
                        delete("/posts/%d".formatted(targetId)))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Post deletedPost = postRepository.findById(targetId).get();
        assertThat(deletedPost.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("게시글 상세 조회 - 삭제된 게시글")
    void t11() throws Exception {
        // given
        Post post = postService.write("테스트 제목", "테스트 내용", 1L, 1L);
        postService.delete(post.getId());

        // when
        ResultActions resultActions = mvc
                .perform(get("/posts/%d".formatted(post.getId())))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("게시판별 글 목록 조회 - 삭제된 게시판")
    void t12() throws Exception {
        // given: BaseInitData에서 4번 게시판이 삭제된 상태

        // when
        ResultActions resultActions = mvc
                .perform(get("/boards/4/posts"))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
