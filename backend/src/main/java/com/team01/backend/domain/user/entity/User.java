package com.team01.backend.domain.user.entity;

import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column // [신규] 리프레시 토큰 저장을 위한 필드
    private String refreshToken;

    @Builder
    public User(String email, String password, String nickname, String profileImage, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role != null ? role : Role.USER;
    }

    public void updateInfo(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * [신규] 회원 탈퇴: 역할을 WITHDRAWN으로 변경
     * BaseEntity의 @PreUpdate에 의해 modifiedAt에 탈퇴 시점이 기록됩니다.
     */
    public void withdraw() {
        this.role = Role.WITHDRAWN;
        this.refreshToken = null; // 탈퇴 시 지휘소(토큰) 파기
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
	
		// User.java 내부 추가
	public void updateProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
}