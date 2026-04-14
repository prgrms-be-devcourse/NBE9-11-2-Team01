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
<<<<<<< HEAD
    private String profileImage; // 필드 추가
=======
>>>>>>> ce107cdc8936de4556fc5c5a454b0e832de7364a
}