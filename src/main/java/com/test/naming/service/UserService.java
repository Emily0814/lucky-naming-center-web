package com.test.naming.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.naming.dto.UserDTO;
import com.test.naming.dto.mapper.UserMapper;
import com.test.naming.entity.User;
import com.test.naming.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor		//Lombok이 final 필드를 위한 생성자를 자동 생성
@Transactional(readOnly = true)	//기본적으로 읽기 전용 트랜잭션 설정
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final BCryptPasswordEncoder passwordEncoder;
	
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
	public User registerUser(UserDTO userDTO) {
		//이메일 중복 확인
		if (isEmailExists(userDTO.getEmail())) {
			throw new RuntimeException("이미 사용중인 이메일입니다.");
		}
		//비밀번호 암호화
		userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		
		//DTO를 Entity로 변환 > 직접 빌드하지 않고 매퍼 사용
		User user = userMapper.toEntity(userDTO);
				
		//저장 및 반환
		return userRepository.save(user);
	}
}
