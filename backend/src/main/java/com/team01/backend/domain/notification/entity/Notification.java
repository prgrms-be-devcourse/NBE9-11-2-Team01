package com.team01.backend.domain.notification.entity;

import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Notification extends BaseEntity {
    private Long receiverId; //받는 사람 (userId)
    private Long senderId; //보내는 사람 (userId)
    private Long targetId; //url
    private String content; // 알림 내용
    private boolean isRead = false;
    //private enum type;

    public Notification(Long receiverId, Long senderId, Long targetId, String content){
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.targetId = targetId;
        this.content = content;
    }
    public void read(){
        this.isRead = true;
    }

}
