package com.test.naming.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.naming.entity.User;
import com.test.naming.repository.UserRepository;
import com.test.naming.security.jwt.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request,
            Authentication authentication) {
        
        // 1. Spring Security 인증 확인
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("인증된 사용자: {}", authentication.getName());
            
            User user = userRepository.findByEmail(authentication.getName());
            if (user != null) {
                Map<String, Object> userInfo = createUserInfoMap(user);
                return ResponseEntity.ok(userInfo);
            }
        }
        
        // 2. JWT 토큰 확인 (헤더)
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        // 3. JWT 토큰 확인 (쿠키)
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        
        // 4. 토큰이 있으면 유효성 검증 및 사용자 정보 반환
        if (token != null) {
            try {
                String userEmail = jwtUtil.extractUsername(token);
                User user = userRepository.findByEmail(userEmail);
                
                if (user != null) {
                    Map<String, Object> userInfo = createUserInfoMap(user);
                    return ResponseEntity.ok(userInfo);
                }
            } catch (Exception e) {
                log.error("토큰 검증 오류: {}", e.getMessage());
            }
        }
        
        // 5. 인증 실패
        return ResponseEntity.ok(Map.of("authenticated", false));
    }
    
    // 사용자 정보를 맵으로 변환하는 헬퍼 메서드
    private Map<String, Object> createUserInfoMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("authenticated", true);
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        // 민감한 정보는 제외
        return userInfo;
    }
}