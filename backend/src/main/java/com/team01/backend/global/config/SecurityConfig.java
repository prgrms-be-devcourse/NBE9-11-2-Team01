package com.team01.backend.global.config;


import com.team01.backend.global.security.CustomAccessDeniedHandler;
import com.team01.backend.global.security.CustomAuthenticationEntryPoint;
import com.team01.backend.global.security.JwtAuthenticationFilter;
import com.team01.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * [과제] 시스템 전체의 보안 설정을 관리하는 클래스입니다.
 * 세션 방식을 사용하지 않고 JWT(Stateless) 방식을 사용하도록 설정했습니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler; //인가 실패 응답 관리
    private final CustomAuthenticationEntryPoint authenticationEntryPoint; //인증 실패 응답 관리
    /**
     * [과제] 비밀번호 암호화를 위한 Encoder 빈 등록입니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [과제] 인증 매니저 빈 등록입니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * [과제] 보안 필터 체인 설정입니다. 가장 핵심적인 부분입니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // [과제] REST API이므로 CSRF 보안은 비활성화합니다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // [과제] 세션을 사용하지 않습니다.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // 로그인, 회원가입은 모두 허용합니다.
                .requestMatchers(HttpMethod.GET, "/boards/**").permitAll()  // board는 로그인 없이 가능하므로 허용하기
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated() // 그 외의 요청은 인증이 필요합니다.
            )
            // [과제] JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 실행되도록 설정합니다.
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            //인증 실패 및 인가 실패 응답 관리
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint)
            );

        return http.build();
    }
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("https://cdpn.io", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}