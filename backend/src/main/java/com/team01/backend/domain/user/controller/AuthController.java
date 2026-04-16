package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.LoginRequest;
import com.team01.backend.domain.user.dto.SignUpRequest;
import com.team01.backend.domain.user.service.AuthService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid; // @Valid 어노테이션 사용을 위해 임포트하였습니다.
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [과제] 인증 관련 HTTP 요청을 받아 처리하는 컨트롤러 클래스입니다.
 * API 응답 규격은 ApiResponse 클래스를 사용하여 통일하였습니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

     // [회원가입 API]
     // @Valid 어노테이션을 사용하여 요청 객체의 유효성 검사를 활성화하였습니다.
     
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원가입 완료", null));
    }

    
    //[로그인 API]
    //Spring Security의 AuthenticationManager를 통해 인증을 수행하고 세션을 저장합니다.
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request, // 입구에서 유효성 검사를 실시합니다.
            HttpServletRequest httpRequest, 
            HttpServletResponse httpResponse) {
        
        // 시큐리티 인증 전, 서비스 계층에서 비즈니스 검증(형식 등)을 선행하여 500 에러를 방지합니다.
        authService.login(request);
        
        // 실제 인증(아이디/비밀번호 대조) 과정을 수행합니다.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // 인증된 정보를 시큐리티 컨텍스트에 저장하여 로그인 상태를 유지합니다.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        
        return ResponseEntity.ok(new ApiResponse<>(true, null, "로그인 성공", null));
    }
}