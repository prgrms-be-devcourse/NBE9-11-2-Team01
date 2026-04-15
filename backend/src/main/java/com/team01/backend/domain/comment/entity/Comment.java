package com.team01.backend.domain.comment.entity;

import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;       // 대댓글이면 부모 댓글, 일반 댓글이면 null

    private boolean isDeleted = false;

    private int likeCount = 0;

    // 댓글 생성할 때 쓰는 생성자
    public Comment(Post post, User user, String content, Comment parent) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.parent = parent;
    }

    // 초기 데이터용 생성자 추가
    public Comment(Post post, String content, Comment parent) {
        this.post = post;
        this.content = content;
        this.parent = parent;  // null이면 일반 댓글, 값 있으면 대댓글
    }

    // 댓글 수정할 때 쓰는 메서드
    public void update(String content) {
        this.content = content;
    }
}
