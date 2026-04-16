package com.team01.backend.domain.user.service;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException; // [수정] 표준 JPA 예외 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [과제명: 객체지향적 예외 처리를 적용한 사용자 서비스]
 * 본 클래스는 마이페이지 조회 및 정보 수정을 담당하는 서비스 레이어입니다.
 * 존재하지 않는 리소스 접근 시 EntityNotFoundException을 발생시켜 
 * 데이터의 부재를 명확히 정의하도록 설계하였습니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * [마이페이지 조회]
     * 식별자(Email)를 통해 사용자를 조회하며, 존재하지 않을 경우 엔티티 미발견 예외를 던집니다.
     */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일을 가진 사용자 정보를 찾을 수 없습니다: " + email));

        return MyPageResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .build();
    }

    /**
     * [사용자 정보 수정]
     * 닉네임과 비밀번호를 변경하며, 대상 사용자가 없을 시 예외를 발생시킵니다.
     */
    public void updateUserInfo(String email, UserUpdateInfoRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("수정 대상 사용자 정보를 찾을 수 없습니다."));

        String finalPassword = user.getPassword();

        // 새로운 비밀번호가 입력된 경우에만 암호화 및 갱신 수행
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            finalPassword = passwordEncoder.encode(request.getNewPassword());
        }
        
        user.updateInfo(request.getNickname(), finalPassword);
    }

    /**
     * [프로필 이미지 수정]
     */
    public void updateProfileImage(String email, UserProfileImageRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("이미지 수정 대상 사용자 정보를 찾을 수 없습니다."));

        user.updateProfileImage(request.getProfileImage());
    }
}