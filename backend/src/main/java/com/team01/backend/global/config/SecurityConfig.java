package com.team01.backend.global.config;

import com.team01.backend.global.security.CustomAccessDeniedHandler;
import com.team01.backend.global.security.CustomAuthenticationEntryPoint;
import com.team01.backend.global.security.JwtAuthenticationFilter;
import com.team01.backend.global.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;



import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, 
                          CustomAccessDeniedHandler accessDeniedHandler, 
                          CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/**").permitAll()      
				.requestMatchers("/static/images/**").permitAll() // 프로필 이미지 보급로
				.requestMatchers("/h2-console/**").permitAll()
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated()                    // /users/ 등은 인증 필요
			/*
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/**").permitAll() // /api/auth/** -> /auth/**
				.requestMatchers(HttpMethod.GET, "/boards/**").permitAll()
				.requestMatchers("/static/images/**").permitAll() // 프로필 이미지 보급로
				.requestMatchers("/h2-console/**").permitAll()
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
				*/
			)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint)
            );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://cdpn.io"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
	


	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
