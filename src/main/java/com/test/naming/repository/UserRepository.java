package com.test.naming.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.naming.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	//이메일로 사용자 찾기
	User findByEmail(String email);

	//이메일 존재 여부 찾기
	boolean existsByEmail(String email);
	
	//닉네임으로 사용자 찾기
	Optional<User> findByNickname(String nickname);
	
	//소셜 로그인 소스와 이메일로 사용자 찾기
	Optional<User> findBySignupSourceAndEmail(String signupSource, String email);
	
	//상태별 사용자 목록 조회 > 필요한지 모르겠음
	List<User> findByStatus(String status);

	
}
