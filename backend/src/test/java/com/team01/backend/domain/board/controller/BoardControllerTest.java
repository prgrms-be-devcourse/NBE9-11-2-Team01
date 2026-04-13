package com.team01.backend.domain.board.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class BoardControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("게시판 목록 조회 - 성공")
    void t1() throws Exception {
        // given: 테스트 데이터가 이미 있다고 가정
        // (또는 @BeforeEach로 데이터 삽입)

        // when
        ResultActions resultActions = mvc
                .perform(get("/board"))
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(BoardController.class))
                .andExpect(handler().methodName("getAllBoards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("게시판 목록 조회 - 빈 배열 반환")
    void t2() throws Exception {
        // given: DB에 게시판 없음

        // when
        ResultActions resultActions = mvc
                .perform(get("/board"))
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(BoardController.class))
                .andExpect(handler().methodName("getAllBoards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
