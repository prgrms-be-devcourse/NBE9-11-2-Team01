package com.team01.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

 // 마이페이지 정보 수정 시 사용하는 데이터 모델입니다.
 // 닉네임과 함께 새로운 비밀번호도 입력받을 수 있도록 설계했습니다.

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateInfoRequest {

    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    private String nickname; // 변경할 닉네임

    private String profileImage; // 변경할 프로필 이미지 (선택)

    // 비밀번호 변경을 위한 필드입니다. 
    // 수정 시에도 보안을 위해 최소 4글자 이상이어야 합니다.

	// @NotBlank를 제거하여 선택 사항으로 변경하되, 입력 시 최소 길이는 검증
    @Size(min = 4, message = "비밀번호는 최소 4글자 이상이어야 합니다.")
    private String newPassword; 
}