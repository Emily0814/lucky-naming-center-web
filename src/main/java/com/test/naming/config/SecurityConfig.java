package com.test.naming.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.test.naming.entity.Token;
import com.test.naming.entity.User;
import com.test.naming.repository.TokenRepository;
import com.test.naming.repository.UserRepository;
import com.test.naming.security.jwt.JwtAuthenticationFilter;
import com.test.naming.security.jwt.JwtUtil;
import com.test.naming.security.oauth.CustomOAuth2UserService;
import com.test.naming.security.oauth.OAuth2LoginSuccessHandler;
import com.test.naming.security.service.CustomUserDetailsService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService; // UserService 대신 CustomUserDetailsService 사용
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        //허가 URL
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll() // JWT 인증 관련 엔드포인트 허용
            .requestMatchers("/mypage").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/", "/index", "/signup", "/login", "/signup-page", "/login-page", "/api/**", "/generator", "/process", "/about").permitAll()
            .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/error/**").permitAll() // 모든 정적 리소스 허용
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
                //추가: 403 접근 거부 처리
                .accessDeniedPage("/error/403")
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
                    // 사용자 정보 가져오기
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    User user = userRepository.findByEmail(userDetails.getUsername());
                    
                    if (user == null) {
                        response.sendRedirect("/?error=true&message=사용자를 찾을 수 없습니다");
                        return;
                    }
                    
                    // JWT 토큰 생성
                    String accessToken = jwtUtil.generateToken(userDetails);
                    String refreshToken = jwtUtil.generateRefreshToken(userDetails);
                    
                    // 토큰 DB에 저장 - 기존 토큰이 있으면 삭제 후 저장
                    tokenRepository.deleteByUser(user);
                    
                    Token token = Token.builder()
                            .user(user)
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .expiresAt(LocalDateTime.now().plusHours(24))
                            .createdAt(LocalDateTime.now())
                            .build();
                    tokenRepository.save(token);
                    
                    // 응답 헤더에 JWT 토큰 추가
                    response.addHeader("Authorization", "Bearer " + accessToken);
                    
                    // 쿠키에 JWT 토큰 저장 (클라이언트에서 접근 가능하도록)
                    Cookie cookie = new Cookie("jwt_token", accessToken);
                    cookie.setPath("/");
                    cookie.setHttpOnly(false); // 자바스크립트에서 접근 가능하도록
                    cookie.setMaxAge(3600); // 1시간
                    response.addCookie(cookie);
                    
                    // 세션이나 요청에서 리다이렉트로 URL 확인
                    String redirectUrl = request.getParameter("redirect");
                    if (redirectUrl == null || redirectUrl.isEmpty()) {
                        response.sendRedirect("/?login_success=true");
                    } else {
                        response.sendRedirect(redirectUrl + "?login_success=true");
                    }
                }
            })
            .permitAll()
        )
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // API 엔드포인트에 대해 CSRF 보호 비활성화
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

        // JWT 인증을 위한 설정 추가
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        // JWT 인증 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService); // CustomUserDetailsService 사용
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}