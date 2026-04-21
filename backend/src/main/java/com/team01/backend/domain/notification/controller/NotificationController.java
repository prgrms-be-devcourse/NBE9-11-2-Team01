package com.team01.backend.domain.notification.controller;

import com.team01.backend.domain.notification.dto.NotificationReadResponseDto;
import com.team01.backend.domain.notification.dto.NotificationResponseDto;
import com.team01.backend.domain.notification.service.NotificationService;
import com.team01.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getNotification(
           @AuthenticationPrincipal UserDetails user
    ){
        List<NotificationResponseDto> notifications = notificationService.getAllNotification(user.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(notifications));
    }
    @PutMapping("/notifications/{notificationId}")

    public ResponseEntity<ApiResponse<NotificationReadResponseDto>> getNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails user
    ){
        NotificationReadResponseDto notification = notificationService.read(notificationId, user.getUsername());

        return ResponseEntity.ok(ApiResponse.ofSuccess(notification));
    }
}
