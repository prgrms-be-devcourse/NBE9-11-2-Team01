package com.team01.backend.domain.user.service;

import com.team01.backend.domain.user.dto.LoginRequest;
import com.team01.backend.domain.user.dto.SignUpRequest;
import com.team01.backend.domain.user.entity.Role;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    //회원가입 - 보안 강화 버전

    public void signUp(SignUpRequest request) {
		// [수정] 이메일 형식 검사 추가
        validateEmailFormat(request.getEmail());
		
		
        // 중복 이메일 체크: GlobalExceptionHandler가 400 에러 유도
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // 권한 결정 및 엄격한 토큰 검증
        Role role = Role.USER; // 기본값
        final String ADMIN_KEY = "user_admin-2026"; // 비밀 키

        // 관리자 토큰이 입력되었다면 검증 실시
        if (request.getAdminToken() != null && !request.getAdminToken().isBlank()) {
            if (request.getAdminToken().equals(ADMIN_KEY)) {
                role = Role.ADMIN;
            } else {
                // 토큰이 틀리면 유저 가입 허용하지 않고 차단
                throw new IllegalArgumentException("관리자 인증 토큰이 일치하지 않습니다.");
            }
        }

        // 3. 무결성이 확인된 대원 정보 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImage(request.getProfileImage())
                .role(role)
                .build();

        userRepository.save(user);
    }

    

    // 로그인 - Global Handler가 아는 예외로 우회
     
	@Transactional(readOnly = true)
	public void login(LoginRequest request) {
        // [1] 이메일 체크
        validateEmailFormat(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 일치하지 않습니다."));

	// [2] 비밀번호 체크 
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			// 이 예외는 GlobalExceptionHandler의 handleIllegalArgument 메서드 사용
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    // 이메일 형식을 직접 확인하는 보안 메서드
    private void validateEmailFormat(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다. ('@' 누락)");
        }
    }
}