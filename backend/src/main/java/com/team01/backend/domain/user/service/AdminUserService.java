package com.team01.backend.domain.user.service;

import com.team01.backend.domain.user.dto.UserResponseDto;
import com.team01.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<UserResponseDto> getAllUser(){
        return userRepository.findAll().stream()
                .map(UserResponseDto::new).toList();
    }
}
