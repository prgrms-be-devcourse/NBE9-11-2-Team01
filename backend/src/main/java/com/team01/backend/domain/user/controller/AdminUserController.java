package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.UserResponseDto;
import com.team01.backend.domain.user.service.AdminUserService;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Tag(name = "관리자 회원 관리", description = "관리자 회원 관리 관련 API")
@RestController
@RequiredArgsConstructor
public class AdminUserController {

    final AdminUserService adminUserService;

    @Operation(summary = "회원 목록 조회", description = "회원목록과 각 회원의 정보를 조회합니다. 관리자 권한에서만 가능합니다.")
    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUser(){
        List<UserResponseDto> users = adminUserService.getAllUser();
        return ResponseEntity.ok(ApiResponse.ofSuccess(users));
    }
}
