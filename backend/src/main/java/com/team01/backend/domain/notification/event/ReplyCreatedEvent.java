package com.team01.backend.domain.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class ReplyCreatedEvent {
    private final Long postId; // post Id
    private final Long commentId; // comment Id
    private final Long commentWriterId; //댓글 작성자 userId
    private final Long replyId; // 답글 Id
    private final Long replyWriterId; // 답글 작성자 userId
    private final String replyContent; // 알림 내용 + 답글 내용
}