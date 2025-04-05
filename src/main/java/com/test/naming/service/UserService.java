package com.test.naming.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.naming.dto.UserDTO;
import com.test.naming.dto.mapper.UserMapper;
import com.test.naming.entity.OAuth;
import com.test.naming.entity.Role;
import com.test.naming.entity.User;
import com.test.naming.repository.OAuthRepository;
import com.test.naming.repository.RoleRepository;
import com.test.naming.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor		//Lombok이 final 필드를 위한 생성자를 자동 생성
@Transactional(readOnly = true)	//기본적으로 읽기 전용 트랜잭션 설정
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final BCryptPasswordEncoder passwordEncoder;
	private final OAuthRepository oAuthRepository;
	private final RoleRepository roleRepository;
	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	
	//사용자 저장(쓰기 작업이므로 readOnly = false)
	@Transactional
	public UserDTO saveUser(UserDTO userDTO) {
		//DTO를 엔티티로 변환
		User user = userMapper.toEntity(userDTO);
		
		//엔티티 저장
		User savedUser = userRepository.save(user);
		
		//저장된 엔티티를 DTO로 변환하여 변환
		return userMapper.toDTO(savedUser);
	}
	
	//사용자 조회 메서드
	public UserDTO findUserById(Long id) {
		return userRepository.findById(id)
				.map(userMapper::toDTO)
				.orElseThrow(() -> new RuntimeException("User not found with id: "+ id));
	}
	
	//이메일 존재 여부 확인
	public boolean isEmailExists(String email) {
		return userRepository.existsByEmail(email);
	}
	
	//이메일로 사용자 찾기
	public UserDTO findUserByEmail(String email) {
		User user = userRepository.findByEmail(email);
		return user != null ? userMapper.toDTO(user) : null;
	}
	
	//사용자 삭제
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	//회원가입(쓰기 작업)
	@Transactional
	public User registerUser(UserDTO userDTO) {
		//이메일 중복 확인
		if (isEmailExists(userDTO.getEmail())) {
			throw new RuntimeException("이미 사용중인 이메일입니다.");
		}
		//비밀번호 암호화
		userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		
		//DTO를 Entity로 변환 > 직접 빌드하지 않고 매퍼 사용
		User user = userMapper.toEntity(userDTO);
		
		// 로그 추가
	    log.info("사용자 저장 전 역할 확인: {}", 
	        user.getRoles() != null ? 
	        user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")) : 
	        "역할 없음");
		
		//사용자 저장 및 결과 반환 (이 단계에서 User_Role 테이블에 데이터 저장되어야 함)
	    User savedUser = userRepository.save(user);
	    
	    //로그 추가
	    log.info("사용자 저장 후 역할 확인: {}", 
	        savedUser.getRoles() != null ? 
	        savedUser.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")) : 
	        "역할 없음");
	    
	    // 필요하다면 영속성 컨텍스트 강제 플러시
	    // entityManager.flush();
		
		//저장 및 반환
		return savedUser;
	}
	
	@Transactional
	public User registerOAuth2User(UserDTO userDTO, String provider) {
	    // 기본값 설정 (필요한 경우)
	    if (userDTO.getStatus() == null) {
	        userDTO.setStatus(1); // 활성 상태
	    }
	    
	    // 소셜 로그인은 비밀번호가 실제로 사용되지 않지만, 데이터베이스 제약 조건을 위해 인코딩
	    userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
	    
	    //UserMapper를 사용하여 사용자 엔티티 생성 (이 과정에서 역할 설정됨)
	    User user = userMapper.toEntity(userDTO);
	    //사용자 저장
	    User savedUser = userRepository.save(user);
	    
	    // OAuth 테이블에 정보 저장
	    OAuth oAuth = OAuth.builder()
	        .user(savedUser)
	        .oauthEmail(userDTO.getEmail())
	        .provider(provider)
	        .createdAt(LocalDateTime.now())
	        .updateAt(LocalDateTime.now())
	        .build();
	    
	    oAuthRepository.save(oAuth);
	    
	    return savedUser;
	    
	}
}
