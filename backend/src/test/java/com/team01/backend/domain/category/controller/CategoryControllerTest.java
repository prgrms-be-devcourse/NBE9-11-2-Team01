package com.team01.backend.domain.category.controller;

import com.team01.backend.global.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
public class CategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    String adminToken = "";
    String user1Token = "";

    @BeforeEach
    void setToken(){
        adminToken = jwtTokenProvider.createToken("admin@admin.com", "ROLE_ADMIN");
        user1Token = jwtTokenProvider.createToken("user1@test.com", "ROLE_USER");
    }

    @Test
    @DisplayName("카테고리 생성 테스트")
    void c1() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "boardId": 1,
                                            "name":"카테고리 4"
                                       }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("createCategory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(6))
                .andExpect(jsonPath("$.data.boardId").value(1))
                .andExpect(jsonPath("$.data.name").value("카테고리 4"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("카테고리 생성 테스트 - null")
    void c2() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name":"카테고리 2"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("createCategory"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message",startsWith("입력값이 올바르지 않습니다.")));
    }
    @Test
    @DisplayName("카테고리 생성 테스트 - 이름 최소 글자수")
    void c3() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "boardId": 1,
                                            "name":"6"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("createCategory"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message",startsWith("입력값이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("카테고리 생성 테스트 - 없는 게시판에 등록")
    void c4() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "boardId": 11,
                                            "name":"category 2"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("createCategory"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message",startsWith("요청하신 데이터를 찾을 수 없습니다")));
    }
    @Test
    @DisplayName("카테고리 생성 테스트 - 게시판 별 이름 중복")
    void c5() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "boardId": 1,
                                            "name":"카테고리 1"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("createCategory"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("중복된 이름입니다"));
    }
    @Test
    @DisplayName("카테고리 생성 - 다른게시판에만 존재하는 이름 - 성공")
    void c6() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "boardId": 3,
                                            "name":"카테고리 1"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("createCategory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.boardId").value(3))
                .andExpect(jsonPath("$.data.name").value("카테고리 1"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("카테고리 생성 테스트 - 권한 없음 ")
    void c7() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(user1Token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "boardId": 1,
                                            "name":"카테고리 6"
                                       }
                                       """)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message",startsWith("권한이 없습니다.")));
    }
    @Test
    @DisplayName("카테고리 수정 테스트")
    void u1() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.put("/admin/categories/1")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name":"카테고리 1 - 수정"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("updateCategory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.boardId").value(1))
                .andExpect(jsonPath("$.data.name").value("카테고리 1 - 수정"))
                .andExpect(jsonPath("$.data.modifiedAt").exists());
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - null")
    void u2() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.put("/admin/categories/2")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("updateCategory"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message",startsWith("입력값이 올바르지 않습니다.")));
    }
    @Test
    @DisplayName("카테고리 수정 테스트 - 없는 id")
    void u3() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.put("/admin/categories/21")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name":"카테고리 21"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("updateCategory"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message",startsWith("요청하신 데이터를 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 이름 중복 체크")
    void u4() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.put("/admin/categories/2")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name":"카테고리 1"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("updateCategory"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message",startsWith("중복된 이름입니다")));
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 권한 없음 ")
    void u5() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.put("/admin/categories/1")
                                .header("Authorization", "Bearer %s".formatted(user1Token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name":"카테고리 1 - 수정"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message",startsWith("권한이 없습니다.")));
    }

    @Test
    @DisplayName("카테고리 관리자 조회 테스트")
    void v1() throws  Exception{
        //
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.get("/admin/categories")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("viewCategory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[4]").exists());
    }

    @Test
    @DisplayName("카테고리 관리자 조회 테스트 - 권한 없음 ")
    void v2() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.get("/admin/categories")
                )
                .andDo(print());
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message",startsWith("인증이 필요합니다.")));
    }
}
