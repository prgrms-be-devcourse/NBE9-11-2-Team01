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

    @Column // 프로필 이미지 경로 (nullable 허용)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String email, String password, String nickname, String profileImage, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role != null ? role : Role.USER; // 기본값 설정
    }
	
	
	public void updateInfo(String nickname, String password) {
		this.nickname = nickname;
		this.password = password;
	}

	public void updateProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
}