package com.test.naming.security.oauth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            log.info("OAuth2 로그인 성공: email={}, provider={}", oAuth2User.getEmail(), oAuth2User.getProvider());
        }
        
        // 기본 성공 처리 (저장된 요청이 있으면 해당 URL로, 없으면 기본 URL로)
        //super.onAuthenticationSuccess(request, response, authentication); > 상위 클래스의 기본 성공 처리만 호출 > 아래 코드로 수정        
        
        //세션에 저장된 리다이렉트 URL이 있는지 확인
        String redirectUrl = (String) request.getSession().getAttribute("REDIRECT_URL");
        
        if (redirectUrl != null) {
            // 세션에서 리다이렉트 URL 제거
            request.getSession().removeAttribute("REDIRECT_URL");
            response.sendRedirect(redirectUrl);
        } else {
            // 기본 URL(홈 페이지)로 리다이렉트
            response.sendRedirect("/");
        }
    }
}