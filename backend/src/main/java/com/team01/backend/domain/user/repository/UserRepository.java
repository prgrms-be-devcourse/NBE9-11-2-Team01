package com.team01.backend.domain.user.repository;

import com.team01.backend.domain.user.entity.Role;
import com.team01.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // 탈퇴하지 않은 사용자만 이메일로 조회 (로그인, 비밀번호 재설정 등에 사용)
    Optional<User> findByEmailAndRoleNot(String email, Role role);
    
    // 탈퇴하지 않은 사용자만 닉네임으로 조회 (아이디 찾기에 사용)
    Optional<User> findByNicknameAndRoleNot(String nickname, Role role);

    // 중복 체크나 전체 관리를 위한 이메일 조회
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}