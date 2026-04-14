package com.team01.backend.domain.category.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("카테고리 생성 테스트")
    void c1() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        MockMvcRequestBuilders.post("/admin/categories")
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

}
