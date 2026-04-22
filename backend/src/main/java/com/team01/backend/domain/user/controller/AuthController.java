package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.service.AuthService;
import com.team01.backend.domain.user.service.MailService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * [과제] 사용자의 요청을 가장 먼저 받는 컨트롤러입니다.
 * 세션 방식이 아닌 JWT 토큰을 응답 본문에 담아 반환하는 구조로 개편하였습니다.
 * [최종 업데이트] 환경별 보안 설정 및 이메일 인증 기능이 통합되었습니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MailService mailService;

    @Value("${custom.cookie.secure:true}")
    private boolean isSecure;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        // ApiResponse 구조: (success, code, message, data)
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원가입 완료", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenDto tokenDto = authService.login(request);
        setCookie(response, "accessToken", tokenDto.getAccessToken(), 60 * 60);
        setCookie(response, "refreshToken", tokenDto.getRefreshToken(), 14 * 24 * 60 * 60);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "로그인 완료", null));
    }

    // [신규] 이메일 인증 코드 발송
    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<Void>> sendCode(@RequestParam String email) {
        mailService.sendVerificationCode(email);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "인증 코드가 발송되었습니다.", null));
    }

    // [신규] 인증 코드 검증 (수정 완료: code 자리에 null, data 자리에 isValid 배치)
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Boolean>> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = mailService.verifyCode(email, code);
        // ApiResponse 생성자(boolean, String, String, T) 순서에 맞춤
        return ResponseEntity.ok(new ApiResponse<>(true, null, "검증 완료", isValid));
    }
	
	/**
     * 아이디 찾기 API: 닉네임을 통해 이메일 정보를 반환합니다.
     */
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<String>> findId(@Valid @RequestBody FindIdRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, authService.findId(request), "아이디 찾기 완료", null));
    }
	

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "비밀번호 재설정 완료", null));
    }

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
                .secure(isSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}