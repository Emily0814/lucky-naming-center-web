package com.test.naming.security.jwt;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.test.naming.security.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 정적 리소스 요청은 JWT 인증 건너뛰기
        if (isStaticResourceRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // JWT 토큰 확인
        String jwt = null;
        
        try {
            // 1. 먼저 Authorization 헤더에서 토큰 확인
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                log.debug("JWT 토큰을 헤더에서 찾음");
            }
            
            // 2. 헤더에 토큰이 없으면 쿠키에서 확인
            if (jwt == null) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwt_token".equals(cookie.getName())) {
                            jwt = cookie.getValue();
                            log.debug("JWT 토큰을 쿠키에서 찾음");
                            break;
                        }
                    }
                }
            }
            
            // 3. 토큰이 없으면 다음 필터로 진행
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // 토큰에서 사용자 이메일 추출
            String userEmail = null;
            try {
                userEmail = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.error("JWT 토큰에서 사용자 이름 추출 실패: {}", e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }
            
            // 이메일이 있고 현재 인증이 설정되지 않은 경우
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = null;
                try {
                    userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                } catch (Exception e) {
                    log.error("사용자 정보 로드 실패: {}", e.getMessage());
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // 토큰이 유효하면 인증 객체 생성
                try {
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        
                        // 인증 설정
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("사용자 인증 완료: {}", userEmail);
                    }
                } catch (Exception e) {
                    log.error("JWT 인증 오류: {}", e.getMessage());
                    // 인증 실패 시 인증 컨텍스트를 명시적으로 초기화
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            log.error("JWT 필터에서 예외 발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
    
    // 정적 리소스 요청인지 확인하는 메서드
    private boolean isStaticResourceRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") || 
               path.startsWith("/fonts/") ||
               path.startsWith("/favicon.ico");
    }
}