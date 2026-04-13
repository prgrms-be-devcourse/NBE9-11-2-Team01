package com.team01.backend.domain.board.controller;

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
    @DisplayName("게시판 생성 테스트")
    void t1() throws  Exception{

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/admin/board")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "게시판 이름 1",
                                            "description":"게시판 설명 1"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BoardController.class))
                .andExpect(handler().methodName("createBoard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        resultActions
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("게시판 이름 1"))
                .andExpect(jsonPath("$.data.description").value("게시판 설명 1"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

}
