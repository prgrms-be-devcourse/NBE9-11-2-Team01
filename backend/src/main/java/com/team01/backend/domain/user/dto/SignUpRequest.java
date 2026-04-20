package com.team01.backend.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, message = "보안을 위해 최소 8글자 이상으로 설정하게.") 
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    private String profileImage;
    
    // [중요 수정] boolean -> Boolean으로 변경하여 null 처리가 가능하도록 수정
    // 기본값을 false로 지정하여 데이터 누락 시 안전하게 처리함
    @Builder.Default
    private Boolean admin = false; 
    
    private String adminToken;

    // 안전한 isAdmin 메서드
    public boolean isAdmin() {
        return this.admin != null && this.admin;
    }
}