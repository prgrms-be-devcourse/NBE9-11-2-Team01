package com.team01.backend.domain.user.dto;

import com.team01.backend.domain.user.entity.Role;
import com.team01.backend.domain.user.entity.User;

public record UserResponseDto(
        String email,
        String nickname,
        String profileImage,
        Role role
) {
    public UserResponseDto(User user) {
        this(
            user.getEmail(),
            user.getNickname(),
            user.getProfileImage(),
            user.getRole()
        );
    }
}
