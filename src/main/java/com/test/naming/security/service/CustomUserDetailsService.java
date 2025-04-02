package com.test.naming.security.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.naming.entity.Role;
import com.test.naming.entity.User;
import com.test.naming.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("로그인 시도: 이메일={}", username);
        
        // Spring Security는 username으로 로그인하지만 여기서는 이메일을 username으로 사용합니다
        User user = userRepository.findByEmail(username);
        if (user == null) {
            log.warn("이메일에 해당하는 사용자가 없음: {}", username);
            throw new UsernameNotFoundException("이메일에 해당하는 사용자가 없습니다: " + username);
        }

        // 사용자 상태 확인 (1=활성, 0=비활성)
        if (user.getStatus() != 1) {
            log.warn("비활성화된 계정: {}", username);
            throw new UsernameNotFoundException("비활성화된 계정입니다: " + username);
        }

        // 비밀번호 디버깅 (실제 운영 환경에서는 제거해야 함)
        log.debug("사용자 비밀번호 해시: {}", user.getPassword().substring(0, 10) + "...");
        
        log.info("사용자 로그인 성공: {}", username);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    // 사용자 권한을 Spring Security의 GrantedAuthority로 변환
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            log.debug("사용자 권한 추가: {}", role.getName());
        }
        
        return authorities;
    }
}