package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.service.UserService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
	
	
 // 사용자 개인 정보와 관련된 API들을 모아둔 컨트롤러입니다.
 // 모든 요청은 JWT 토큰 헤더(Authorization: Bearer <token>)가 필요합니다.
	
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
	
	// 마이페이지 내 정보 보기
	// ApiResponse 규격: (success, code, message, data)

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(Authentication authentication) {
        // 인증 객체에서 이메일을 꺼내 서비스에 전달한다네.
        MyPageResponse response = userService.getMyPage(authentication.getName());
        
        // 정해진 규격에 맞춰 데이터를 마지막 파라미터(data)에 배치했네.
        return ResponseEntity.ok(new ApiResponse<>(true, null, "정보를 성공적으로 가져왔습니다.", response));
    }

     // 닉네임 및 비밀번호 수정 (정보 수정 버튼)
    @PatchMapping("/me/info")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            @Valid @RequestBody UserUpdateInfoRequest request, Authentication authentication) {
        
        userService.updateUserInfo(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "성공적으로 변경되었습니다.", null));
    }


     // 프로필 이미지만 수정 (이미지 변경 버튼)
    @PatchMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> updateProfileImage(
            @RequestBody UserProfileImageRequest request,
            Authentication authentication) {
        
        userService.updateProfileImage(authentication.getName(), request); //
        return ResponseEntity.ok(new ApiResponse<>(true, null, "이미지가 성공적으로 변경되었습니다.", null));
    }
}