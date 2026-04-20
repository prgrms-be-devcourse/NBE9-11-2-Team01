package com.team01.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 아이디 찾기(JSER-04)를 위한 DTO입니다.

@Getter @NoArgsConstructor @AllArgsConstructor
public class FindIdRequest {
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;
}