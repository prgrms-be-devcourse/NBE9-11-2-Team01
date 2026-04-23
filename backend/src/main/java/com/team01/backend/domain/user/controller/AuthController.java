package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.service.AuthService;
import com.team01.backend.domain.user.service.MailService;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth") // 클래스 수준에 적용하여 모든 메서드에 인증 필요 표시
public class AuthController {

    private final AuthService authService;
    private final MailService mailService;

    @Value("${custom.cookie.secure:true}")
    private boolean isSecure;

    @Operation(summary = "회원가입", description = "유저 정보를 받아 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        // ApiResponse 구조: (success, code, message, data)
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원가입 완료", null));
    }

    @Operation(summary = "로그인", description = "유저 정보를 받아 로그인처리합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenDto tokenDto = authService.login(request);
        setCookie(response, "accessToken", tokenDto.getAccessToken(), 15 * 60);        // 15분
        setCookie(response, "refreshToken", tokenDto.getRefreshToken(), 7 * 24 * 60 * 60); // 7일
        return ResponseEntity.ok(new ApiResponse<>(true, null, "로그인 완료", null));
    }

    // [신규] 이메일 인증 코드 발송
    @Operation(summary = "이메일 인증 코드 발송", description = "이메일을 받아 인증번호를 발송합니다.")
    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<Void>> sendCode(@RequestParam String email) {
        mailService.sendVerificationCode(email);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "인증 코드가 발송되었습니다.", null));
    }

    // [신규] 인증 코드 검증 (수정 완료: code 자리에 null, data 자리에 isValid 배치)
    @Operation(summary = "이메일 인증 코드 검증", description = "인증번호를 보낸 번호와 비교해 검증합니다.")
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Boolean>> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = mailService.verifyCode(email, code);
        // ApiResponse 생성자(boolean, String, String, T) 순서에 맞춤
        return ResponseEntity.ok(new ApiResponse<>(true, null, "검증 완료", isValid));
    }
	
	/**
     * 아이디 찾기 API: 닉네임을 통해 이메일 정보를 반환합니다.
     */
    @Operation(summary = "아이디 찾기", description = "닉네임을 받아 유저 이메일(아이디)를 찾습니다.")
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<String>> findId(@Valid @RequestBody FindIdRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, null, "아이디 찾기 완료", authService.findId(request)));
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 재설정합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "비밀번호 재설정 완료", null));
    }
    @Operation(summary = "탈퇴", description = "회원 탈퇴합니다.")
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(Authentication authentication, HttpServletResponse response) {
        authService.withdraw(authentication.getName());
        setCookie(response, "accessToken", "", 0);
        setCookie(response, "refreshToken", "", 0);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원 탈퇴 완료", null));
    }
    @Operation(summary = "토큰 재발급", description = "refresh token으로 access token을 재발급합니다.")
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
    @Operation(summary = "로그아웃", description = "토큰을 초기화해 로그아웃 처리합니다.")
	@PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication, HttpServletResponse response) {
        authService.logout(authentication.getName());
        // [수정] null 대신 빈 문자열 ""을 사용하여 예외 방지
        setCookie(response, "accessToken", "", 0);
        setCookie(response, "refreshToken", "", 0);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "로그아웃 성공", null));
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