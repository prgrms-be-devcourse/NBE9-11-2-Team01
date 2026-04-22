package com.team01.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [수정] Redis 의존성을 제거하고 메모리 기반 Map을 사용하는 인증 서비스
 */
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    
    // [변경] RedisTemplate 대신 스레드 안전한 ConcurrentHashMap 사용
    // private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, String> verificationStore = new ConcurrentHashMap<>();

    // 인증 코드 발송 및 메모리 저장
    public void sendVerificationCode(String email) {
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        
        // [변경] Redis 대신 로컬 맵에 저장
        verificationStore.put(email, code);

        // 메일 발송 로직
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("security-center@local.com");
        message.setSubject("[TEST] 본인 확인을 위한 인증 코드입니다.");
        message.setText("요청하신 인증 코드는 [" + code + "] 입니다.\n테스트 환경이므로 즉시 입력해 주십시오.");
		
		// 콘솔 출력으로 즉시 확인 가능하게 함
		System.out.println("==========================================");
		System.out.println("[LOCAL TEST] 대상 이메일: " + email);
		System.out.println("[LOCAL TEST] 생성된 인증 코드: " + code);
		System.out.println("==========================================");
        
        mailSender.send(message);
    }

    // 인증 코드 검증 (메모리 조회)
    public boolean verifyCode(String email, String code) {
        // [변경] Map에서 코드 조회
        String savedCode = verificationStore.get(email);
        
        if (code != null && code.equals(savedCode)) {
            // 인증 성공 시 보안을 위해 즉시 삭제
            verificationStore.remove(email);
            return true;
        }
        return false;
    }
}
