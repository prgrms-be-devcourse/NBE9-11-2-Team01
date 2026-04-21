package com.team01.backend.domain.post.entity;

import com.team01.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * 좋아요 이력 테이블
 *
 * ✅ UNIQUE(user_id, post_id) 제약:
 *    동시 요청으로 중복 INSERT 시도가 오더라도 DB 레벨에서 차단.
 *    서비스 레이어의 exists 체크 + save 사이의 Race Condition 방어.
 */
@Getter
@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_likes_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        }
)
@NoArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
