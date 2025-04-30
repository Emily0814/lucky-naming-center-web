package com.test.naming.security.oauth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.test.naming.entity.Token;
import com.test.naming.entity.User;
import com.test.naming.repository.TokenRepository;
import com.test.naming.repository.UserRepository;
import com.test.naming.security.jwt.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        String token = null;
        
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            log.info("OAuth2 로그인 성공: email={}, provider={}", oAuth2User.getEmail(), oAuth2User.getProvider());
        
            // JWT 토큰 생성
            token = jwtService.generateToken(oAuth2User);
            
            // Authorization 헤더에 JWT 토큰 추가
            response.addHeader("Authorization", "Bearer " + token);
            
            // 쿠키에 JWT 토큰 저장 (클라이언트에서 접근 가능하도록)
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setPath("/");
            cookie.setHttpOnly(false); // 자바스크립트에서 접근 가능하도록
            cookie.setMaxAge(3600); // 1시간
            response.addCookie(cookie);
            
            // 토큰을 데이터베이스에 저장 (별도 트랜잭션으로 처리)
            try {
                saveTokenToDatabase(oAuth2User.getEmail(), token);
            } catch (Exception e) {
                log.error("토큰 저장 중 오류 발생: {}", e.getMessage());
                // 토큰 저장에 실패해도 로그인 프로세스는 계속 진행
            }
        }
        
        // 리다이렉트 URL 설정
        String finalRedirectUrl;
        
        // 세션에 저장된 리다이렉트 URL이 있는지 확인
        String redirectUrl = (String) request.getSession().getAttribute("REDIRECT_URL");
        
        if (redirectUrl != null) {
            // 세션에서 리다이렉트 URL 제거
            request.getSession().removeAttribute("REDIRECT_URL");
            finalRedirectUrl = redirectUrl;
        } else {
            // 기본 URL(홈 페이지)로 리다이렉트
            finalRedirectUrl = "/";
        }
        
        // 로그인 성공 파라미터 추가
        finalRedirectUrl += (finalRedirectUrl.contains("?") ? "&" : "?") + "login_success=true";
        
        log.info("리다이렉트 URL: {}", finalRedirectUrl);
        response.sendRedirect(finalRedirectUrl);
    }
    
    // 토큰을 데이터베이스에 저장하는 메서드 (별도 트랜잭션으로 처리)
    @Transactional
    public void saveTokenToDatabase(String email, String token) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            log.error("토큰 저장 실패: 사용자를 찾을 수 없음 - {}", email);
            return;
        }
        
        // 기존 토큰이 있으면 삭제 - 삭제 쿼리 별도 실행으로 변경
        tokenRepository.deleteByUser(user);
        
        // 만료 시간 계산
        LocalDateTime accessExpiry = LocalDateTime.now().plusHours(24); // 액세스 토큰은 24시간
        
        // 토큰 저장
        Token tokenEntity = Token.builder()
                .user(user)
                .accessToken(token)
                .refreshToken(null) // 리프레시 토큰은 없음
                .expiresAt(accessExpiry)
                .createdAt(LocalDateTime.now())
                .build();
        
        tokenRepository.save(tokenEntity);
        log.info("토큰이 데이터베이스에 저장되었습니다: {}", email);
    }
}