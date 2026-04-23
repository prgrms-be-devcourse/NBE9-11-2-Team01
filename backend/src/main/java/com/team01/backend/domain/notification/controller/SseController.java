package com.team01.backend.domain.notification.controller;

import com.team01.backend.domain.notification.repository.SseEmitterRepository;
import com.team01.backend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
@Tag(name = "실시간 알림", description = "실시간 알림 연결 관련 API")
@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterRepository sseEmitterRepository;
    private final UserService userService;

    @Operation(summary = "실시간 알림 연결", description = "실시간 알림을 보내기 위해 클라이언트와 최초 연결합니다. 로그인한 상태에서만 가능합니다.")
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetails user) { // 연결 생성하는 역할, 실시간 알림 받기 위해 일단 연결하는 역할

        // UserDetail에서 userId 가져오기
        String username = user.getUsername();
        Long userId = userService.findIdByUsername(username);

        // 클라이언트와 연결 유지 객체(emitter) 생성
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간

        sseEmitterRepository.save(userId, emitter);

        // 알림 생성하면 안되는 경우
        emitter.onCompletion(() -> sseEmitterRepository.delete(userId, emitter));
        emitter.onTimeout(() -> sseEmitterRepository.delete(userId, emitter));
        emitter.onError((e) -> sseEmitterRepository.delete(userId, emitter));

        // SSE 연결 알림 보내기 (연결 확인용)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitter.complete();
        }

        return emitter;
    }
}