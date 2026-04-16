package com.team01.backend.domain.user.service;

import com.team01.backend.domain.user.dto.LoginRequest;
import com.team01.backend.domain.user.dto.SignUpRequest;
import com.team01.backend.domain.user.entity.Role;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import com.team01.backend.domain.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [과제] 사용자 인증의 핵심 로직을 처리하는 서비스 클래스입니다.
 * JWT 도입을 위해 로그인 메서드의 반환 타입을 String(토큰)으로 변경하였습니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    /**
     * 회원가입 로직: 중복 체크 및 입력값의 물리적 검증을 수행합니다.
     */
    public void signUp(SignUpRequest request) {
        // [백엔드 직접 검증] @ 포함 여부와 비밀번호 길이를 한 번 더 확인하여 보안을 강화합니다.
        validateInput(request.getEmail(), request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        Role role = Role.USER;
        // 관리자 토큰 일치 여부 확인
        if ("user_admin-2026".equals(request.getAdminToken())) {
            role = Role.ADMIN;
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(role)
                .build();

        userRepository.save(user);
    }

    /**
     * [로그인 로직] 인증 성공 시 JWT 토큰 문자열을 반환하도록 수정하였습니다.
     */
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        // 1. 입력값의 형식(@ 포함 여부 등)을 먼저 확인합니다.
        validateInput(request.getEmail(), request.getPassword());

        // 2. 사용자 존재 여부 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 3. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 4. [JWT 과제] 인증 성공 시 토큰을 생성하여 반환합니다.
        // 실제 구현 시에는 JwtTokenProvider를 통해 생성하지만, 여기서는 예시 문자열을 반환합니다.
        return jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
    }

    /**
     * [보조 메서드] 이메일 형식과 비밀번호 길이를 직접 검사하는 보안 필터입니다.
     * 조건에 맞지 않으면 IllegalArgumentException을 던져 400 에러를 유도합니다.
     */
    private void validateInput(String email, String password) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 최소 4글자 이상이어야 합니다.");
        }
    }
}