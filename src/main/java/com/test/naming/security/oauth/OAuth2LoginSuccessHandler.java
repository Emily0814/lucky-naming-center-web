package com.test.naming.security.oauth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.test.naming.security.jwt.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	// JwtService 주입
    private final JwtService jwtService;
	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            log.info("OAuth2 로그인 성공: email={}, provider={}", oAuth2User.getEmail(), oAuth2User.getProvider());
        
        
        // JWT 토큰 생성
        String token = jwtService.generateToken(oAuth2User);
        
        // Authorization 헤더에 JWT 토큰 추가
        response.addHeader("Authorization", "Bearer " + token);
        
        // 쿠키에 JWT 토큰 저장 (클라이언트에서 접근 가능하도록)
        Cookie cookie = new Cookie("jwt_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(false); // 자바스크립트에서 접근 가능하도록
        cookie.setMaxAge(3600); // 1시간
        response.addCookie(cookie);
        
    	}
        // 기본 성공 처리 (저장된 요청이 있으면 해당 URL로, 없으면 기본 URL로)
        //super.onAuthenticationSuccess(request, response, authentication); > 상위 클래스의 기본 성공 처리만 호출 > 아래 코드로 수정        
        
        //세션에 저장된 리다이렉트 URL이 있는지 확인
        String redirectUrl = (String) request.getSession().getAttribute("REDIRECT_URL");
        
        // 리다이렉트 URL에 토큰 정보를 쿼리 파라미터로 추가 (프론트엔드에서 처리 가능하도록)
        String finalRedirectUrl;
        
        if (redirectUrl != null) {
            // 세션에서 리다이렉트 URL 제거
            request.getSession().removeAttribute("REDIRECT_URL");
            //response.sendRedirect(redirectUrl);
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
}