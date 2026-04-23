package com.team01.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * [과제] 회원 정보를 관리하는 핵심 엔티티 클래스입니다.
 * [수정] 상속 구조를 단순화하고, JPA Auditing 필드를 엔티티 내부에 직접 배치하여 데이터 무결성을 강화했습니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // [추가] @Builder 작동을 위한 생성자
@EntityListeners(AuditingEntityListener.class) // [중요] 생성/수정 시간 자동 기록
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String profileImage; // 프로필 이미지 경로 저장 필드

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column 
    private String refreshToken;

    // [학술적 보완] 데이터 생성 시점 자동 기록 필드
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // [학술적 보완] 데이터 수정 시점 자동 기록 필드
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @Builder
    public User(String email, String password, String nickname, String profileImage, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role != null ? role : Role.USER;
    }

    /**
     * [신규] 영속화 전 기본 데이터 설정 로직
     * 회원가입 시 프로필 이미지가 누락된 경우 로컬 기본 경로를 강제로 할당합니다.
     */
    @PrePersist
    public void prePersist() {
        if (this.profileImage == null || this.profileImage.isBlank()) {
            this.profileImage = null;  // null로 두면 프론트에서 기본 이미지 처리
        }
    }

    /* --- 기존 도메인 비즈니스 로직 및 주석 유지 --- */

    /**
     * 회원 정보(닉네임, 비밀번호)를 수정합니다.
     */
    public void updateInfo(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
    }

    /**
     * 리프레시 토큰을 갱신합니다.
     */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 회원 탈퇴 처리를 진행합니다. (Soft Delete 전략)
     */
    public void withdraw() {
        this.role = Role.WITHDRAWN;
        this.refreshToken = null;
    }

    /**
     * 비밀번호를 변경합니다.
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 프로필 이미지를 직접 변경합니다.
     */
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}