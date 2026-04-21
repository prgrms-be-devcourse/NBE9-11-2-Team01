package com.team01.backend.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


 // 비밀번호 재설정(JSER-03)을 위한 DTO입니다. 
@Getter @NoArgsConstructor @AllArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4글자 이상이어야 합니다.")
    private String newPassword;
}