package com.team01.backend.domain.board.controller;

import com.team01.backend.domain.user.dto.LoginRequest;
import com.team01.backend.domain.user.service.AuthService;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AdminBoardControllerTest {
    @Autowired
    private MockMvc mvc;

    String adminToken = "";
    String user1Token = "";

    @Test
    @DisplayName("게시판 생성 테스트")
    void t1() throws  Exception{

        ResultActions resultActions = mvc
                .perform(
                        post("/admin/boards")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
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
                .andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("createBoard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        resultActions
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("게시판 이름 1"))
                .andExpect(jsonPath("$.data.description").value("게시판 설명 1"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }
    @Test
    @DisplayName("게시판 생성 테스트 - null")
    void t2() throws  Exception{

        ResultActions resultActions = mvc
                .perform(
                        post("/admin/boards")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "description":"게시판 설명 1"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("createBoard"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다. (name: must not be null)"));
    }
    @Test
    @DisplayName("게시판 생성 테스트 - 최소 길이 충족 X")
    void t3() throws  Exception{

        ResultActions resultActions = mvc
                .perform(
                        post("/admin/boards")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "1",
                                            "description":"설명1"                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("createBoard"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message",startsWith("입력값이 올바르지 않습니다.")))
                .andExpect(jsonPath("$.message",containsString("name: size")))
                .andExpect(jsonPath("$.message",containsString("description: size")));
    }

    @Test
    @DisplayName("게시판 생성 테스트 - 중복된 이름")
    void t4() throws  Exception{

        ResultActions resultActions = mvc
                .perform(
                        post("/admin/boards")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "name1",
                                            "description":"description 1"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("createBoard"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message",startsWith("중복된 이름입니다")));
    }
    @Test
    @DisplayName("게시판 생성 테스트 - 일반 유저 로그인")
    void t5() throws  Exception{

        ResultActions resultActions = mvc
                .perform(
                        post("/admin/boards")
                                .header("Authorization", "Bearer %s".formatted(user1Token))
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }
    @Test
    @DisplayName("게시판 생성 테스트 - 로그인 안된 상태")
    void t6() throws  Exception{

        ResultActions resultActions = mvc
                .perform(
                        post("/admin/boards")
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    // 게시판 수정 테스트
    @Test
    @DisplayName("게시판 수정 테스트")
    void u1() throws Exception{
        int targetId = 1;
        ResultActions resultActions = mvc
                .perform(
                        put("/admin/boards/%s".formatted(targetId))
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "name 1 modified",
                                            "description":"description1"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions.andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("updateBoard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("name 1 modified"))
                .andExpect(jsonPath("$.data.description").value("description1"))
                .andExpect(jsonPath("$.data.modifiedAt").exists());
    }
    @Test
    @DisplayName("게시판 수정 테스트 - null")
    void u2() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        put("/admin/boards/2")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name":null,
                                            "description":"게시판 설명 1"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions.andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("updateBoard"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message",startsWith("입력값이 올바르지 않습니다.")));
    }
    @Test
    @DisplayName("게시판 수정 테스트 - 없는 id")
    void u3() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        put("/admin/boards/6")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name":"name6",
                                            "description":"description 6"
                                        }
                                        """)
                )
                .andDo(print());
        resultActions.andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("updateBoard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message",startsWith("요청하신 데이터를 찾을 수 없습니다.")));
    }

    @Test
    @DisplayName("게시판 삭제 테스트")
    void d1() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        delete("/admin/boards/3")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                )
                .andDo(print());
        resultActions.andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("deleteBoard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    @Test
    @DisplayName("게시판 삭제 테스트 - 없는 id")
    void d2() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        delete("/admin/boards/6")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                )
                .andDo(print());
        resultActions.andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("deleteBoard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message",startsWith("요청하신 데이터를 찾을 수 없습니다.")));
    }
    @Test
    @DisplayName("게시판 삭제 테스트 - 삭제된 게시판")
    void d3() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        delete("/admin/boards/4")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                )
                .andDo(print());
        resultActions.andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("deleteBoard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message",startsWith("요청하신 데이터를 찾을 수 없습니다.")));
    }


    @Test
    @DisplayName("게시판 다건 조회 테스트")
    void v1() throws Exception{
        ResultActions resultActions = mvc
                .perform(
                        get("/admin/boards")
                                .header("Authorization", "Bearer %s".formatted(adminToken))
                )
                .andDo(print());
        resultActions.andExpect(handler().handlerType(AdminBoardController.class))
                .andExpect(handler().methodName("getBoards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[2]").exists());
    }

}
