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
 * [과제명: Spring Security와 JWT 기반의 웹 보안 시스템 구축]
 * 본 클래스는 사용자 인증(Authentication)과 권한 부여(Authorization)를 위한 
 * 핵심 비즈니스 로직을 처리하는 서비스 레이어입니다.
 * 회원가입 시의 비밀번호 해싱 처리와 로그인 성공 시의 토큰 발행을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * [구현 포인트] JWT 생성을 전담하는 공통 컴포넌트입니다.
     * 로그인 성공 시 클라이언트에게 제공할 액세스 토큰을 발행하는 데 사용됩니다.
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * [메서드: signUp]
     * 신규 사용자의 정보를 시스템에 등록합니다.
     * 중복 가입 방지를 위해 이메일 존재 여부를 우선 검증합니다.
     */
    public void signUp(SignUpRequest request) {
        // [검증] 입력된 이메일과 비밀번호의 유효성을 체크합니다.
        validateInput(request.getEmail(), request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // [보안] 비밀번호는 BCrypt 알고리즘을 사용하여 단방향 암호화 후 저장합니다.
        Role role = Role.USER;
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(role)
                .build();

        userRepository.save(user);
    }

    /**
     * [메서드: login]
     * 사용자 인증을 수행하고 최종적으로 JWT 토큰을 반환합니다.
     * @return 생성된 JWT 액세스 토큰 (String)
     */
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        // 1. 입력 데이터 필터링
        validateInput(request.getEmail(), request.getPassword());

        // 2. 사용자 계정 존재 여부 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 3. 암호화된 비밀번호와 입력값 대조 (matches 메서드 활용)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        /**
         * [수정 사항: 타입 불일치 해결]
         * JwtTokenProvider.createToken의 시그니처가 String 타입을 요구하므로,
         * 기존의 List.of()를 제거하고 사용자의 Role 이름(String)을 직접 전달합니다.
         * 이를 통해 'incompatible types' 컴파일 에러를 해결하였습니다.
         */
        return jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
    }

    /**
     * [유효성 검사 모듈]
     * 서비스 계층에서의 2차 검증을 통해 시스템의 안정성을 확보합니다.
     */
    private void validateInput(String email, String password) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 최소 4자 이상이어야 합니다.");
        }
    }
}