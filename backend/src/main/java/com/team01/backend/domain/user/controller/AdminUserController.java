package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.UserResponseDto;
import com.team01.backend.domain.user.service.AdminUserService;
import com.team01.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminUserController {

    final AdminUserService adminUserService;

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUser(){
        List<UserResponseDto> users = adminUserService.getAllUser();
        return ResponseEntity.ok(ApiResponse.ofSuccess(users));
    }
}
