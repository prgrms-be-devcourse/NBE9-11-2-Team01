package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.LoginRequest;
import com.team01.backend.domain.user.dto.SignUpRequest;
import com.team01.backend.domain.user.service.AuthService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid; // [과제] 유효성 검증 활성화를 위해 필수입니다.
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [과제] 사용자의 요청을 가장 먼저 받는 컨트롤러입니다.
 * 세션 방식이 아닌 JWT 토큰을 응답 본문에 담아 반환하는 구조로 개편하였습니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API: @Valid를 통해 DTO의 검증 로직을 실행합니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원가입 완료", null));
    }

    /**
     * 로그인 API: 성공 시 발급된 JWT 토큰을 ApiResponse의 데이터 필드에 담아 응답합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        // 1. 서비스에서 인증을 수행하고 생성된 JWT 토큰을 전달받습니다.
        // 형식이 틀리거나 인증 실패 시 IllegalArgumentException이 발생하여 400 에러가 나갑니다.
        String token = authService.login(request);
        
        // 2. 발급된 토큰을 클라이언트에게 성공 메시지와 함께 반환합니다.
        return ResponseEntity.ok(new ApiResponse<>(true, token, "로그인 성공", null));
    }
}