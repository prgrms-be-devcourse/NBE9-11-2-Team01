package com.team01.backend.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자"),
	WITHDRAWN("ROLE_WITHDRAWN", "회원탈퇴");

    private final String key;
    private final String title;
}