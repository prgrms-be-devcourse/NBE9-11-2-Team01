package com.team01.backend.domain.user.dto;

import lombok.*;

 // 프로필 사진만 따로 수정할 때 사용하는 DTO입니다.
 // 사진 업로드 버튼이 따로 있는 UI 구조에 맞춰 독립적으로 설계했습니다.

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileImageRequest {
    private String profileImage; // 변경할 이미지의 URL 혹은 파일 경로
}