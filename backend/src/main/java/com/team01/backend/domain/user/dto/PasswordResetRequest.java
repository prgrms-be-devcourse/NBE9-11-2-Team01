package com.team01.backend.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "인증 코드는 필수 입력 값입니다.")
    private String verificationCode;

    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8글자 이상이어야 합니다.")
    private String newPassword;
}