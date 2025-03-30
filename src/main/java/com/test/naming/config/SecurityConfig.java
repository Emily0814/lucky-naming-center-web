package com.test.naming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
            .requestMatchers("/", "/index", "/signup", "/login").permitAll()	// 메인 경로를 허용
            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 모든 정적 리소스 허용
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .permitAll()
        ); 
        
    return http.build();
	}
}
