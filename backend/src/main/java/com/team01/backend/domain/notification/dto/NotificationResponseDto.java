package com.team01.backend.domain.notification.dto;

import com.team01.backend.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponseDto(
     Long id,
     Long receiverId, //받는 사람 (userId)
     Long senderId, //보내는 사람 (userId)
     Long targetId, //url
     String content, // 알림 내용
     boolean isRead,
     LocalDateTime createdAt
){
    public NotificationResponseDto(Notification notification){
        this(
                notification.getId(),
                notification.getReceiverId(),
                notification.getSenderId(),
                notification.getTargetId(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

}
