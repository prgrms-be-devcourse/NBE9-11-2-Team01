package com.team01.backend.domain.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * [과제] JWT 토큰을 생성하고 유효성을 검증하는 '토큰 관리자' 클래스입니다.
 * 보안을 위해 비밀키(Secret Key)는 암호화하여 관리합니다.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // [과제] 토큰 서명에 사용할 비밀키입니다. 실제 서비스에서는 환경변수로 관리해야 합니다.
    private String secretKey = "human-evolution-project-secret-key-for-jwt-token-2026";
    private Key key;

    // [과제] 토큰 유효 시간은 1시간(3600000ms)으로 설정하였습니다.
    private final long tokenValidTime = 60 * 60 * 1000L;

    private final UserDetailsService userDetailsService;

    // [과제] 객체 초기화 시 비밀키를 Base64로 인코딩하여 저장합니다.
    @PostConstruct
    protected void init() {
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(encodedKey.getBytes());
    }

    /**
     * [과제] 사용자의 이메일과 권한을 받아 JWT 토큰을 생성합니다.
     */
    public String createToken(String userEmail, String role) {
        Claims claims = Jwts.claims().setSubject(userEmail);
        claims.put("role", role); // 토큰 내부에 사용자 권한 정보를 저장합니다.
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 토큰 발행 시간
                .setExpiration(new Date(now.getTime() + tokenValidTime)) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘 설정
                .compact();
    }

    /**
     * [과제] 토큰에서 인증 정보를 조회합니다.
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * [과제] 토큰에서 사용자의 아이디(이메일)를 추출합니다.
     */
    public String getUserPk(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * [과제] HTTP 요청 헤더에서 토큰 값을 가져옵니다. ("Authorization" : "Bearer {TOKEN}")
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * [과제] 토큰의 유효성과 만료일자를 확인합니다.
     */
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}