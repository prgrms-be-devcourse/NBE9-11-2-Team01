package com.team01.backend.domain.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCreatedEvent {
    private final Long postId; // post Id
    private final Long postOwnerId; //post author userId
    private final Long commentId; // comment Id
    private final Long commentWriterId; // 댓글 작성자 userId
    private final String commentContent; // 알림 내용 + 댓글 내용
}