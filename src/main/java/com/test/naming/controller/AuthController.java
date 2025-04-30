package com.test.naming.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.naming.dto.AuthRequest;
import com.test.naming.dto.AuthResponse;
import com.test.naming.entity.Token;
import com.test.naming.entity.User;
import com.test.naming.repository.TokenRepository;
import com.test.naming.repository.UserRepository;
import com.test.naming.security.jwt.JwtUtil;
import com.test.naming.security.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService; // UserService 대신 CustomUserDetailsService 사용
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // 인증 성공 시 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 사용자 정보 가져오기
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername());
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "사용자를 찾을 수 없습니다.",
                        "success", "false"
                ));
            }
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            // 토큰 저장
            saveToken(user, accessToken, refreshToken);
            
            // 응답 데이터 생성
            AuthResponse response = AuthResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            
            log.info("로그인 성공: {}", user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.",
                    "success", "false"
            ));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "리프레시 토큰이 필요합니다.",
                    "success", "false"
            ));
        }
        
        try {
            // 토큰에서 사용자 이메일 추출
            String userEmail = jwtUtil.extractUsername(refreshToken);
            
            // 이메일로 사용자 정보 조회
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            
            // 토큰 유효성 검증
            if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "리프레시 토큰이 유효하지 않습니다.",
                        "success", "false"
                ));
            }
            
            // 새 액세스 토큰 생성
            String newAccessToken = jwtUtil.generateToken(userDetails);
            
            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "success", "true"
            ));
            
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "토큰 갱신에 실패했습니다.",
                    "success", "false"
            ));
        }
    }
    
    private void saveToken(User user, String accessToken, String refreshToken) {
        // 기존 사용자의 토큰이 있으면 삭제 (선택적)
        // tokenRepository.deleteByUser(user);
        
        // 만료 시간 계산 (애플리케이션 설정값과 일치하도록 수정 필요)
        LocalDateTime accessExpiry = LocalDateTime.now().plusHours(24); // 액세스 토큰은 24시간
        
        // 토큰 저장
        Token token = Token.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(accessExpiry)
                .createdAt(LocalDateTime.now())
                .build();
        
        tokenRepository.save(token);
    }
}