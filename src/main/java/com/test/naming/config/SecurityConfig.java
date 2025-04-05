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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.test.naming.security.oauth.CustomOAuth2UserService;
import com.test.naming.security.oauth.OAuth2LoginSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

//1. 비밀번호 인코딩 방식 정의
//2. URL 접근 권한 설정
//3. 로그인/로그아웃 처리 설정
//4. CSRF 보호 설정
//5. 세션 관리 등의 보안 기능 정의

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        //허가 URL
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/mypage").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/", "/index", "/signup", "/login", "/signup-page", "/login-page", "/api/**").permitAll()
            .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll() // 모든 정적 리소스 허용
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
            .loginPage("/login-page") // 로그인 페이지 URL
            .loginProcessingUrl("/api/login") // 로그인 처리 URL (form의 action)
            .usernameParameter("username") // 기본은 'username'이지만 코드에서 'email'로 사용하므로 명시적으로 설정
            .passwordParameter("password") // 기본은 'password'
            .failureUrl("/?error=true&login=failed") // 로그인 실패 시 메인 페이지로 이동하면서 에러 파라미터 전달
            .successHandler(new AuthenticationSuccessHandler() {
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException {
                    // 세션이나 요청에서 리다이렉트로 URL 확인
                    String redirectUrl = request.getParameter("redirect");
                    if (redirectUrl == null || redirectUrl.isEmpty()) {
                        response.sendRedirect("/");
                    } else {
                        response.sendRedirect(redirectUrl);
                    }
                }
            })
            .permitAll()
        )
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // API 엔드포인트에 대해 CSRF 보호 비활성화 (개발 중에만 사용)
            .ignoringRequestMatchers("/api/**")
        );
        
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login-page")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
            );
        
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }
}