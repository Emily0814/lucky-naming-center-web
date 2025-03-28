package com.test.naming.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.naming.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	//역할 이름으로 역할 조회
	Optional<Role> findByName(String name);
	//역할 이름 존재 여부 확인
	boolean existsByName(String name);
}
