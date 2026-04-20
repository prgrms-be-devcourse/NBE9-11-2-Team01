package com.team01.backend.domain.comment.entity;

import com.team01.backend.domain.user.entity.User;
import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "comment_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_comment_likes_comment_user",
                columnNames = {"comment_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public CommentLike(Comment comment, User user) {
        this.comment = comment;
        this.user = user;
    }
}
