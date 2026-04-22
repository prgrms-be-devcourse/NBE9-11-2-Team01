package com.team01.backend.domain.user.service;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.entity.Role;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import com.team01.backend.global.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
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
	private final MailService mailService;

    /**
     * [메서드: signUp]
     * 관리자 토큰 검증 로직을 포함하여 사용자를 등록합니다.
     */
    @Transactional
    public void signUp(SignUpRequest request) {
        // 1. 입력값 기본 검증
        validateInput(request.getEmail(), request.getPassword());
        
        // [신규] 탈퇴 회원 포함 중복 체크: 이미 우리 시스템을 거쳐 간 모든 데이터를 확인합니다.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중이거나 탈퇴 처리된 이메일입니다.");
        }

        // 2. 관리자 여부 확인 및 권한 할당
        Role role = Role.USER;
        if (request.isAdmin()) {
            // 기획된 관리자 비밀 토큰과 일치하는지 확인 (BaseInitData와 동일값 설정)
            if (!"user_admin-2026".equals(request.getAdminToken())) {
                throw new IllegalArgumentException("관리자 인증 토큰이 일치하지 않습니다.");
            }
            role = Role.ADMIN;
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
    @Transactional
    public TokenDto login(LoginRequest request) {
        validateInput(request.getEmail(), request.getPassword());

        // 1. 전체 사용자에서 이메일 조회 (탈퇴 여부와 관계없이 조회)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // [핵심 추가] 탈퇴한 회원 식별: WITHDRAWN 상태인 경우 명확한 에러 응답을 보냅니다.
        if (user.getRole() == Role.WITHDRAWN) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. 이중 토큰 발행: 액세스 토큰(단기)과 리프레시 토큰(장기)을 동시에 생성합니다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 4. 지휘관 각인: 리프레시 토큰을 DB에 저장하여 추후 세션 관리에 활용합니다.
        user.updateRefreshToken(refreshToken);
        
        return new TokenDto(accessToken, refreshToken);
    }

    /**
     * [신규] 로그아웃 로직
     * 사용자의 리프레시 토큰을 무효화하여 모든 기기에서의 세션 갱신을 차단합니다.
     */
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        // DB의 리프레시 토큰을 제거하여 탈취된 토큰의 사용을 원천 봉쇄합니다.
        user.updateRefreshToken(null);
    }

    /**
     * [신규] 토큰 재발급 로직 (Reissue)
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발행하여 끊김 없는 경험을 제공합니다.
     */
    @Transactional
    public String reissue(String refreshToken) {
        // 1. 토큰 자체의 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다. 다시 로그인하십시오.");
        }

        // 2. 토큰 내 정보로 사용자 식별
        User user = userRepository.findByEmail(jwtTokenProvider.getUserEmail(refreshToken))
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 3. DB에 저장된 토큰과 대조하여 부적절한 갱신 시도를 차단합니다.
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("비정상적인 토큰 갱신 시도입니다.");
        }

        // 4. 새로운 액세스 토큰 발행
        return jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
    }

    /**
     * [신규] 아이디 찾기
     * 탈퇴한 유령 회원을 제외하고 오직 활성 사용자 중에서만 닉네임으로 검색합니다.
     */
    @Transactional(readOnly = true)
    public String findId(FindIdRequest request) {
        User user = userRepository.findByNicknameAndRoleNot(request.getNickname(), Role.WITHDRAWN)
                .orElseThrow(() -> new EntityNotFoundException("해당 정보로 등록된 활성 사용자가 없습니다."));
        return user.getEmail();
    }

	// 비밀번호 재설정 로직
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        // [보안 필수] 인증 코드 검증 단계
        boolean isVerified = mailService.verifyCode(request.getEmail(), request.getVerificationCode());
        if (!isVerified) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않거나 만료되었습니다.");
        }

        User user = userRepository.findByEmailAndRoleNot(request.getEmail(), Role.WITHDRAWN)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없거나 이미 탈퇴한 계정입니다."));
        
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * [신규] 회원 탈퇴 수행 (Soft Delete)
     * 역할을 WITHDRAWN으로 변경하고 모든 인증 정보를 파기하여 영구 격리합니다.
     */
    @Transactional
    public void withdraw(String email) {
        User user = userRepository.findByEmailAndRoleNot(email, Role.WITHDRAWN)
                .orElseThrow(() -> new EntityNotFoundException("활성화된 사용자 정보를 찾을 수 없습니다."));
        
        user.withdraw(); // Role 변경 및 리프레시 토큰 null 처리
    }

    private void validateInput(String email, String password) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }
}