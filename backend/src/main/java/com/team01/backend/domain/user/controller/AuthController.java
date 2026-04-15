package com.team01.backend.domain.user.controller;

import com.team01.backend.domain.user.dto.LoginRequest;
import com.team01.backend.domain.user.dto.SignUpRequest;
import com.team01.backend.domain.user.service.AuthService;
import com.team01.backend.global.response.ApiResponse; //
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody SignUpRequest request) {
        authService.signUp(request);
        // [규격 준수] ApiResponse record 생성자를 직접 사용하여 메시지를 전달합니다.
        return ResponseEntity.ok(new ApiResponse<>(true, null, "회원가입 완료", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @RequestBody LoginRequest request, 
            HttpServletRequest httpRequest, 
            HttpServletResponse httpResponse) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        
        // [규격 준수] ApiResponse record 생성자를 직접 사용하여 메시지를 전달합니다.
        return ResponseEntity.ok(new ApiResponse<>(true, null, "로그인 성공", null));
    }
}