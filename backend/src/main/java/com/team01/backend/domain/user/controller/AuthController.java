package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.service.AuthService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * [과제] 사용자의 요청을 가장 먼저 받는 컨트롤러입니다.
 * 세션 방식이 아닌 JWT 토큰을 응답 본문에 담아 반환하는 구조로 개편하였습니다.
 * [최종 업데이트] 보안 강화를 위해 토큰을 HttpOnly 쿠키에 담아 반환하도록 수정되었습니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원가입 완료", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenDto tokenDto = authService.login(request);
        setCookie(response, "accessToken", tokenDto.getAccessToken(), 3600);
        setCookie(response, "refreshToken", tokenDto.getRefreshToken(), 14 * 24 * 3600);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "로그인 성공", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication, HttpServletResponse response) {
        authService.logout(authentication.getName());
        // [수정] null 대신 빈 문자열 ""을 사용하여 예외 방지
        setCookie(response, "accessToken", "", 0);
        setCookie(response, "refreshToken", "", 0);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "로그아웃 성공", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken, 
            HttpServletResponse response) {
        
        // [신규] 리프레시 토큰 쿠키가 없을 경우에 대한 방어 로직
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("리프레시 토큰이 존재하지 않습니다. 다시 로그인하십시오.");
        }
        
        String newAccessToken = authService.reissue(refreshToken);
        setCookie(response, "accessToken", newAccessToken, 3600);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "토큰 갱신 성공", null));
    }

    // ==========================================
    // [복원됨] 누락되었던 계정 복구 관련 API 2종
    // ==========================================

    /**
     * 아이디 찾기 API: 닉네임을 통해 이메일 정보를 반환합니다.
     */
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<String>> findId(@Valid @RequestBody FindIdRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, authService.findId(request), "아이디 찾기 완료", null));
    }

    /**
     * 비밀번호 재설정 API: 이메일 인증 확인 후 새로운 비밀번호로 변경합니다.
     */
    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "비밀번호 재설정 완료", null));
    }

    // ==========================================

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(Authentication authentication, HttpServletResponse response) {
        authService.withdraw(authentication.getName());
        setCookie(response, "accessToken", "", 0);
        setCookie(response, "refreshToken", "", 0);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원 탈퇴 완료", null));
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true) // 로컬 테스트(HTTP) 시에는 false로 변경 필요
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}