package com.test.naming.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.naming.entity.Role;
import com.test.naming.repository.RoleRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DataInitializer {

    /**
     * 애플리케이션 시작 시 기본 데이터 초기화
     */
    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository) {
        return args -> {
            // 기본 역할 추가 (없는 경우에만)
            if (!roleRepository.existsByName("ROLE_USER")) {
                roleRepository.save(Role.builder()
                        .name("ROLE_USER")
                        .build());
                log.info("기본 사용자 역할(ROLE_USER) 생성됨");
            }
            
            if (!roleRepository.existsByName("ROLE_ADMIN")) {
                roleRepository.save(Role.builder()
                        .name("ROLE_ADMIN")
                        .build());
                log.info("관리자 역할(ROLE_ADMIN) 생성됨");
            }
        };
    }
}