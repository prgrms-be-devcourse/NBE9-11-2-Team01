package com.team01.backend.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    private String adminToken; // [추가] 관리자 권한 획득을 위한 토큰
}