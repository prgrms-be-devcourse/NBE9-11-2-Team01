package com.team01.backend.domain.notification.dto;

import com.team01.backend.domain.notification.entity.Notification;

public record NotificationResponseDto(
     Long receiverId, //받는 사람 (userId)
     Long senderId, //보내는 사람 (userId)
     Long targetId, //url
     String content, // 알림 내용
     boolean isRead
){
    public NotificationResponseDto(Notification notification){
        this(
                notification.getReceiverId(),
                notification.getSenderId(),
                notification.getTargetId(),
                notification.getContent(),
                notification.isRead()
        );
    }

}
