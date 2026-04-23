package com.team01.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * [과제] 정적 리소스 핸들러 설정 클래스입니다.
 * 클라이언트의 /static/images/ 요청을 실제 resources 폴더와 매핑합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/images/**")
                .addResourceLocations("classpath:/static/images/"); 
                // [수정] 'file:' 대신 'classpath:'를 사용하면 resources 폴더 내부를 탐색합니다.
    }
}