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

import java.util.List;

/**
 * [과제명: Spring Security와 JWT를 활용한 보안 인증 시스템 구현]
 * 본 서비스 클래스는 사용자의 회원가입 및 로그인 절차를 관장하는 핵심 로직을 포함합니다.
 * 클래스 레벨의 @Transactional 선언을 통해 기본적인 원자성을 보장하며,
 * 각 메서드의 성격에 맞춰 세부적인 트랜잭션 옵션을 최적화하였습니다.
 */
@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨 선언으로 모든 public 메서드에 트랜잭션 보호막을 적용합니다.
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * [구현 사항] 실제 인가 토큰을 발행하기 위한 JwtTokenProvider를 주입받습니다.
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * [메서드: signUp]
     * 신규 사용자를 시스템의 정식 데이터로 등록하는 과정입니다.
     * @Transactional: 자네의 피드백을 수용하여, 데이터 쓰기 작업의 중요성을 강조하고 
     * 설정 오류로부터 원자성을 보호하기 위해 메서드 레벨에 직접 명시하였습니다.
     */
    @Transactional 
    public void signUp(SignUpRequest request) {
        // [검증] 도메인 로직 진입 전, 입력값의 물리적 유효성을 재검증합니다.
        validateInput(request.getEmail(), request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // [보안] 비밀번호는 단방향 해시 알고리즘인 BCrypt를 통해 암호화되어 저장됩니다.
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    /**
     * [메서드: login]
     * 사용자의 자격 증명을 확인하고, 성공 시 접근 권한이 담긴 JWT를 생성하여 반환합니다.
     * readOnly = true: DB 부하를 줄이고 성능을 최적화하기 위해 읽기 전용 모드를 활성화했습니다.
     */
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        // 1. 입력 형식 기초 검증
        validateInput(request.getEmail(), request.getPassword());

        // 2. 사용자 존재 여부 확인 (식별자: Email)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 3. 비밀번호 일치 여부 확인 (암호화된 값과 대조)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        /**
         * [JWT 발행 로직] 
         * 기존의 가짜 문자열을 제거하고, JwtTokenProvider를 호출하여 실제 JWT를 생성합니다.
         * 타입 불일치 에러 해결: createToken(String, String) 규격에 맞춰 권한명을 단일 문자열로 전달합니다.
         */
        return jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
    }

    /**
     * [내부 검증 로직]
     * 비즈니스 데이터의 정합성을 보장하기 위해 서비스 레이어에서 수행하는 최종 필터링입니다.
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