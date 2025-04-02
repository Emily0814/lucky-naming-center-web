package com.test.naming.dto.mapper;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.test.naming.dto.UserDTO;
import com.test.naming.entity.User;
import com.test.naming.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {	//사용자 관련 엔티티-DTO 변환기

    private final RoleRepository roleRepository;

    //DTO -> Entity 변환
    public User toEntity(UserDTO dto) {
        User user = User.builder()
            .id(dto.getId())
            .email(dto.getEmail())
            .password(dto.getPassword())
            .nickname(dto.getNickname())
            .profile(dto.getProfile())
            .createdAt(dto.getCreatedAt())
            .status(dto.getStatus())
            .build();
        
        //기본 역할을 "ROLE_USER"로 설정
        roleRepository.findByName("ROLE_USER")
        	.ifPresent(role -> {
            if(user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            user.getRoles().add(role);
        });
        
        return user;
    }

    //Entity -> DTO 변환
    public UserDTO toDTO(User entity) {
    	//역할 이름 추출(기본값은 "ROLE_USER")
    	String roles = "ROLE_USER"; //기본값 설정
    	if(entity.getRoles() != null && !entity.getRoles().isEmpty()) {
    		roles = entity.getRoles().iterator().next().getName();
    	}
        return UserDTO.builder()
            .id(entity.getId())
            .nickname(entity.getNickname())
            .email(entity.getEmail())
            .password(entity.getPassword())
            .createdAt(entity.getCreatedAt())
            .status(entity.getStatus())
            .profile(entity.getProfile())
            .roles(roles)
            .build();
    }
}