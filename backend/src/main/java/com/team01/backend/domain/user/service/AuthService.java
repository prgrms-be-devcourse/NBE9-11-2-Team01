package com.team01.backend.domain.user.service;

import com.team01.backend.domain.user.dto.LoginRequest;
import com.team01.backend.domain.user.dto.SignUpRequest;
import com.team01.backend.domain.user.entity.Role;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import com.team01.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [과제명: 권한 계층 구조가 적용된 JWT 인증 시스템]
 * 본 서비스는 사용자의 가입 시점에 관리자 여부를 판별하고,
 * 이에 따른 차등화된 권한(USER/ADMIN)을 부여하는 핵심 로직을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * [메서드: signUp]
     * 관리자 토큰 검증 로직을 포함하여 사용자를 등록합니다.
     */
    @Transactional
    public void signUp(SignUpRequest request) {
        // 1. 입력값 기본 검증
        validateInput(request.getEmail(), request.getPassword());

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        /**
         * [구현 사항: 권한 결정 및 엄격한 관리자 토큰 검증]
         * 가입 요청 시 전달된 adminToken의 유효성을 검사하여 Role을 결정합니다.
         */
        Role role = Role.USER; // 기본 권한은 USER로 설정
        final String ADMIN_KEY = "user_admin-2026"; // 시스템 관리자 전용 비밀 키

        // 관리자 토큰이 입력되었다면 검증 실시
        if (request.getAdminToken() != null && !request.getAdminToken().isBlank()) {
            if (request.getAdminToken().equals(ADMIN_KEY)) {
                role = Role.ADMIN; // 토큰 일치 시 관리자 권한 부여
            } else {
                // 토큰이 틀리면 유저 가입 자체를 차단하여 보안을 강화함
                throw new IllegalArgumentException("관리자 인증 토큰이 일치하지 않습니다.");
            }
        }

        // 3. 사용자 엔티티 생성 및 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(role) // 결정된 권한 주입
                .build();

        userRepository.save(user);
    }

    /**
     * [메서드: login]
     * 인증 성공 시 사용자의 Role 정보가 포함된 실제 JWT를 발행합니다.
     */
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        validateInput(request.getEmail(), request.getPassword());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JwtTokenProvider를 통해 실제 토큰 발행 (사용자의 Role 포함)
        return jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
    }

    private void validateInput(String email, String password) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 최소 4자 이상이어야 합니다.");
        }
    }
}