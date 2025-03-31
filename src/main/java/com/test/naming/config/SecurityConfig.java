package com.test.naming.config;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {	//애플리케이션의 보안 정책을 구현 > Spring Security의 작동 방식을 사용자 정의하는 중요한 클래스
	
//	1. 비밀번호 인코딩 방식 정의
//	2. URL 접근 권한 설정
//	3. 로그인/로그아웃 처리 설정
//	4. CSRF 보호 설정
//	5. 세션 관리 등의 보안 기능 정의

	@Bean 
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		//허가 URL
	    http.authorizeHttpRequests(auth -> auth
	    	.requestMatchers("/mypage").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/", "/index", "/signup", "/login", "/signup-page", "/login-page", "/api/**").permitAll()	// 메인 경로를 허용
            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 모든 정적 리소스 허용
            .anyRequest().authenticated()
        )
	    .exceptionHandling(exception -> exception
	            .authenticationEntryPoint(new AuthenticationEntryPoint() {
	                @Override
	                public void commence(HttpServletRequest request, HttpServletResponse response,
	                        AuthenticationException authException) throws IOException {
	                    // AJAX 요청인지 확인
	                    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
	                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	                        response.getWriter().write("{\"needLogin\":true}");
	                    } else {
	                        // 일반 요청일 경우 로그인이 필요하다는 정보와 함께 원래 URL로 이동
	                        String redirectUrl = request.getRequestURI();
	                        response.sendRedirect("/?needLogin=true&redirectUrl=" + URLEncoder.encode(redirectUrl, "UTF-8"));
	                    }
	                }
	            })
	        )
        .formLogin(form -> form
            .loginPage("/login-page")
            .loginProcessingUrl("/api/login")
            .successHandler(new AuthenticationSuccessHandler() {
            	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            			Authentication authentication) throws IOException {
            		//세션이나 요청에서 리다이렉트로 URL 확인
            		String redirectUrl = request.getParameter("redirect");
            		if (redirectUrl == null || redirectUrl.isEmpty()) {
            			response.sendRedirect("/");
            		} else {
            			response.sendRedirect(redirectUrl);
            		}
            	}
            })
            .permitAll()
        ); 
	    //.csrf(csrf -> csrf.disable());  // API 호출을 위해 CSRF 비활성화 (테스트 환경에서만!)
	    
	    
    return http.build();
	}
}
