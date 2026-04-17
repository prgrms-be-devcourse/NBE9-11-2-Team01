package com.team01.backend.domain.post.controller;

import com.jayway.jsonpath.JsonPath;
import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.post.service.PostService;
import jakarta.persistence.EntityNotFoundException;
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

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Test
    @DisplayName("게시판별 글 목록 조회 - 성공")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/boards/1/posts?page=1&size=20"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("getPostsByBoardId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.posts").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.totalPages").exists())
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.data.hasNext").exists())
                .andExpect(jsonPath("$.data.posts[0].author").exists())
                .andExpect(jsonPath("$.data.posts[0].categoryId").exists())
                .andExpect(jsonPath("$.data.posts[0].categoryName").exists());
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
    @DisplayName("글 수정 성공 - 제목, 내용, 올바른 카테고리 변경")
    void t7_1() throws Exception {
        // 기존 게시글 정보 조회 (연관된 게시판 ID를 얻기 위해서)
        Long targetId = 1L;
        Post targetPost = postRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("대상 게시글 없음"));

        // 해당 게시판에 속한 다른 카테고리를 새롭게 준비 (검증 로직 통과를 위함)
        Long targetBoardId = targetPost.getBoard().getId();
        Category newCategory = categoryRepository.save(new Category(targetBoardId, "수정된 카테고리"));
        Long newCategoryId = newCategory.getId();

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
                                            "content" : "%s",
                                            "categoryId" : %d
                                        }
                                        """.formatted(title, content, newCategoryId))
                )
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postDto.title").value(title))
                .andExpect(jsonPath("$.data.postDto.content").value(content))
                .andExpect(jsonPath("$.data.postDto.categoryId").value(newCategoryId))
                .andExpect(jsonPath("$.data.postDto.categoryName").value("수정된 카테고리"));

        Post post = postRepository.findById(targetId).get();
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getCategory().getId()).isEqualTo(newCategoryId);
    }

    @Test
    @DisplayName("글 수정 실패 - 다른 게시판의 카테고리 ID를 전달한 경우")
    void t7_2() throws Exception {
        // 기존 게시글 준비
        Long targetId = 1L;
        Post targetPost = postRepository.findById(targetId).get();
//        Long originalBoardId = targetPost.getBoard().getId();

        // 다른 게시판, 그 게시판의 카테고리 생성 (예: 공지사항 게시판)
        Board anotherBoard = boardRepository.save(new Board("공지사항", "공지사항 게시판"));
        Category invalidCategory = categoryRepository.save(new Category(anotherBoard.getId(), "공지용 카테고리"));
        Long invalidCategoryId = invalidCategory.getId();


        ResultActions resultActions = mvc
                .perform(
                        put("/posts/%d".formatted(targetId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                            "title" : "수정 시도",
                                            "content" : "내용 수정 시도",
                                            "categoryId" : %d
                                        }
                                        """.formatted(invalidCategoryId))
                )
                .andDo(print());


        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("해당 게시판에서 사용할 수 없는 카테고리입니다."));

        // DB 데이터가 변경되지 않았는지 확인 (Safety Check)
        Post post = postRepository.findById(targetId).get();

        assertThat(post.getCategory().getId()).isNotEqualTo(invalidCategoryId);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 성공")
    void t8() throws Exception {
        String loginResponse = mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "email": "user1@test.com",
                                        "password": "1234"
                                    }
                                    """))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(loginResponse, "$.data");

        ResultActions resultActions = mvc
                .perform(get("/posts/1")
                        .header("Authorization", "Bearer " + token))
                .andDo(print());

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
        String loginResponse = mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "email": "user1@test.com",
                                        "password": "1234"
                                    }
                                    """))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(loginResponse, "$.data");

        ResultActions resultActions = mvc
                .perform(get("/posts/999")
                        .header("Authorization", "Bearer " + token))
                .andDo(print());

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
        String loginResponse = mvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "email": "user1@test.com",
                                        "password": "1234"
                                    }
                                    """))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(loginResponse, "$.data");

        Post post = postService.write("테스트 제목", "테스트 내용", 1L, 1L);
        postService.delete(post.getId());

        ResultActions resultActions = mvc
                .perform(get("/posts/%d".formatted(post.getId()))
                        .header("Authorization", "Bearer " + token))
                .andDo(print());

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

    @Test
    @DisplayName("게시판별 글 목록 조회 - 잘못된 페이지 번호")
    void t13() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/boards/1/posts?page=0"))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
