package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.*;
import com.team01.backend.domain.user.service.UserService;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * [과제명: RESTful API 설계 및 자원 갱신 규격 통일]
 * 본 컨트롤러는 사용자의 자원(Resource)을 관리하는 엔드포인트를 제공합니다.
 * 기획 의도에 따라 리소스의 수정을 @PutMapping으로 단일화하여
 * API의 일관성과 유지보수 용이성을 확보하였습니다.
 */
@Tag(name = "사용자", description = "사용자 정보 관리 API")
@RestController
@RequestMapping("/users") // [수정] /api/users에서 /users로 경로를 단일화했습니다.
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth") // 클래스 수준에 적용하여 모든 메서드에 인증 필요 표시
public class UserController {

    private final UserService userService;

    /**
     * [기능: 마이페이지 조회]
     * 현재 인증된 사용자의 정보를 ApiResponse 규격에 맞춰 반환합니다.
     */
    @Operation(summary = "마이페이지 조회", description = "현재 인증된 사용자의 마이페이지 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(Authentication authentication) {
        MyPageResponse response = userService.getMyPage(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, null, "정보를 성공적으로 가져왔습니다.", response));
    }

    /**
     * [기능: 사용자 정보 갱신]
     * 닉네임과 비밀번호 정보를 수정합니다.
     * [변경사항] 기존 @PatchMapping에서 기획 규격에 맞춰 @PutMapping으로 변경되었습니다.
     */
    @Operation(summary = "사용자 정보 갱신", description = "현재 사용자의 닉네임 또는 비밀번호를 변경합니다.")
    @PutMapping("/me/info")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            @Valid @RequestBody UserUpdateInfoRequest request,
            Authentication authentication) {

        userService.updateUserInfo(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "성공적으로 변경되었습니다.", null));
    }

    /**
     * [기능: 프로필 이미지 갱신]
     * 사용자의 프로필 이미지를 새로운 경로로 업데이트합니다.
     * [변경사항] API 명세 통일을 위해 @PutMapping 어노테이션을 적용하였습니다.
     */
    @Operation(summary = "프로필 이미지 갱신", description = "현재 사용자의 프로필 이미지 경로를 업데이트합니다.")
    @PutMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> updateProfileImage(
            @Valid @RequestBody UserProfileImageRequest request, // [보안강화] @Valid 추가
            Authentication authentication) {

        userService.updateProfileImage(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "이미지가 성공적으로 변경되었습니다.", null));
    }
}