package com.team01.backend.global.config;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 로컬 개발 환경 전용 SMTP 가상 서버 설정
 */
@Configuration
public class MailConfig {

    @Bean
    @Profile("local")
    public GreenMail greenMail() {
        // 3025 포트에서 SMTP 가상 서버 가동
        GreenMail greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
        greenMail.start();
        return greenMail;
    }
}