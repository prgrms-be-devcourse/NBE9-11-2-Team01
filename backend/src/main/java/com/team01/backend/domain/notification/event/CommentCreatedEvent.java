package com.team01.backend.domain.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCreatedEvent {
    private final Long postId; // post Id
    private final Long postOwnerId; //post - author userName
    private final Long commentId; // 댓글 ID
    private final Long commentWriterId; // 댓글 작성자 userName
}