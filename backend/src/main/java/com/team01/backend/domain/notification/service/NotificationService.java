package com.team01.backend.domain.notification.service;

import com.team01.backend.domain.notification.entity.Notification;
import com.team01.backend.domain.notification.event.CommentCreatedEvent;
import com.team01.backend.domain.notification.repository.SseEmitterRepository;
import com.team01.backend.domain.notification.repository.NotificationRepository;
import com.team01.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService { // 알림을 실제로 보내는 역할, 다시 볼 수 있도록 DB에 저장하는 역할

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void handleNotification(CommentCreatedEvent event) {

        // 1. 실제 DB 저장

        Notification notification = notificationRepository.save(
                new Notification(event.getPostOwnerId(), event.getCommentWriterId(),event.getPostId(),"댓글이 달렸습니다.") //targetId를 postId? commentId?
        );

        // 2. SSE 전송
        List<SseEmitter> emitters =
                sseEmitterRepository.findByUserId(event.getPostOwnerId());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(notification);
            } catch (IOException e) {
                emitter.complete();
            }
        }
    }
}
