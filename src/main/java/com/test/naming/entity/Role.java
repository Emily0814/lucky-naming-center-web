package com.test.naming.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ROLE")
@SequenceGenerator(
		name = "ROLE_SEQ_GENERATOR",
		sequenceName = "ROLE_SEQ",
		allocationSize = 1
)
public class Role {	//사용자 권한 정보를 저장(역할 이름과 설명 포함, 사용자와 다대다 관계 설정)

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ROLE_SEQ_GENERATOR")
	@Column(name = "ROLE_ID")
	private Long id;
	
	@Column(name = "NAME", nullable = false, length = 255)
	private String name;
	
	@ManyToMany(mappedBy = "roles")
	private Set<User> users = new HashSet<>();
}
