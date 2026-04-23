package com.team01.backend.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * [과제] 모든 요청에 대해 JWT 토큰이 유효한지 검사하는 '필터' 클래스입니다.
 * Security 필터 체인에서 사용자 인증을 담당합니다.
 * [수정] 헤더 방식에서 HttpOnly 쿠키 방식으로 토큰 추출 로직이 변경되었습니다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. [수정] 쿠키 저장소에서 accessToken을 추출합니다.
        String token = resolveTokenFromCookie(request);

        // 2. 토큰이 유효하다면 인증 정보를 SecurityContext에 저장합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 다음 필터로 요청을 넘깁니다.
        filterChain.doFilter(request, response);
    }

    /**
     * [신규 추가] 쿠키 배열을 뒤져 accessToken 값을 찾아내는 내부 메서드
     */
    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}