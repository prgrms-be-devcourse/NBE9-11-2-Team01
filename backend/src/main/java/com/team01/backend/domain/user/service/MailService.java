package com.team01.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    // 인증 코드 발송 및 Redis 저장 (3분 TTL)
    public void sendVerificationCode(String email) {
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        
        // Redis에 3분간 저장
        redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(3));

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("security-center@local.com");
        message.setSubject("[TEST] 본인 확인을 위한 인증 코드입니다.");
        message.setText("요청하신 인증 코드는 [" + code + "] 입니다.\n3분 이내에 입력해 주십시오.");
		
		
		// [추가] 콘솔에 인증 코드를 출력해서 바로 확인하게 합니다.
		System.out.println("==========================================");
		System.out.println("[LOCAL DEBUG] 대상 이메일: " + email);
		System.out.println("[LOCAL DEBUG] 생성된 인증 코드: " + code);
		System.out.println("==========================================");
        
        mailSender.send(message);
    }

    // 인증 코드 검증 (성공 시 즉시 삭제)
    public boolean verifyCode(String email, String inputCode) {
        String savedCode = redisTemplate.opsForValue().get(email);
        if (savedCode != null && savedCode.equals(inputCode)) {
            redisTemplate.delete(email);
            return true;
        }
        return false;
    }
}