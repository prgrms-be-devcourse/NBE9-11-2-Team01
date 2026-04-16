package com.team01.backend.domain.user.dto;

import lombok.*;

 // 마이페이지 조회 시 사용자 정보를 안전하게 전달하기 위한 DTO입니다.
 // 비밀번호와 같은 민감 정보는 제외하고 화면에 보여줄 정보만 담았습니다.
 
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageResponse {
    private String email;        // 사용자 계정 아이디
    private String nickname;     // 사용자 활동 닉네임
    private String profileImage; // 사용자 프로필 이미지 경로
    private String role;         // 사용자 권한 (USER 또는 ADMIN)
}