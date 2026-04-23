package com.team01.backend.domain.notification.controller;

import com.team01.backend.domain.notification.dto.NotificationReadResponseDto;
import com.team01.backend.domain.notification.dto.NotificationResponseDto;
import com.team01.backend.domain.notification.service.NotificationService;
import com.team01.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Tag(name = "알림", description = "댓글/답글 알림 관련 API")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    @Operation(summary = "알림 조회", description = "알림을 조회합니다. 로그인한 상태에서만 가능합니다.")
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getNotification(
           @AuthenticationPrincipal UserDetails user
    ){
        List<NotificationResponseDto> notifications = notificationService.getAllNotification(user.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(notifications));
    }

    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 처리합니다. 로그인한 상태에서만 가능합니다.")
    @PutMapping("/notifications/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationReadResponseDto>> getNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails user
    ){
        NotificationReadResponseDto notification = notificationService.read(notificationId, user.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(notification));
    }
}
