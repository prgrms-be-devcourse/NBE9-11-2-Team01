package com.team01.backend.domain.user.service;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

 // 사용자 정보(마이페이지) 관련 핵심 로직을 처리하는 서비스입니다.

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

     // [마이페이지 조회] 
     // JWT 토큰에서 추출한 이메일을 사용해 본인의 정보를 가져옵니다.

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        return MyPageResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .build();
    }

    // [닉네임 및 비밀번호 수정]
    // 한 번의 요청으로 닉네임과 비밀번호를 모두 변경합니다.	
	public void updateUserInfo(String email, UserUpdateInfoRequest request) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

		// 비밀번호 암호화하여 저장
		String finalPassword = user.getPassword(); // 기존 비밀번호 유지

		// 새로운 비밀번호가 존재할 때만 암호화 과정을 거치도록 설계했네.
		if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
			finalPassword = passwordEncoder.encode(request.getNewPassword());
		}
		
		// 엔티티의 업데이트 메서드 호출
		user.updateInfo(request.getNickname(), finalPassword);
	}

     // [프로필 이미지 별도 수정]
     // 별도의 버튼 클릭 시 이미지 경로만 즉시 업데이트합니다.
    
    public void updateProfileImage(String email, UserProfileImageRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        user.updateProfileImage(request.getProfileImage());
    }
}