package com.test.naming.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.test.naming.dto.UserDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
@Table(name = "USER")
@SequenceGenerator(
	    name = "USER_SEQ_GENERATOR",  // JPA에서 사용할 시퀀스 이름 (아무거나 가능)
	    sequenceName = "USER_SEQ",  // 실제 DB에 생성된 시퀀스 이름
	    allocationSize = 1
	)
public class User {	//사용자 정보를 저장(사용자 이름, 이메일, 비밀번호, 역할 등 정보 포함, 소셜 로그인 정보와 연결)

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ_GENERATOR")
	@Column(name ="USER_ID")
	private Long id;
	
	@Column(name = "NICKNAME", nullable = false, length=300)
	private String nickname;
	
	@Column(name = "EMAIL", nullable = false, length=300, unique=true)
	private String email;
	
	@Column(name = "PASSWORD", nullable = false, length=300)
	private String password;
	
	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;
    
    @Column(name = "STATUS", nullable = false, length=10)
    private Integer status;

    @Column(name = "PROFILE", length=300)
    private String profile;
	
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "USER_ROLE",
        joinColumns = @JoinColumn(name = "USER_ID"),
        inverseJoinColumns = @JoinColumn(name = "ROLE_ID")
    )
    private Set<Role> roles = new HashSet<>();
    
    // UserDTO 변환 메서드
    public UserDTO toDTO() {
    	String roleString = this.roles.isEmpty() ? "" : this.roles.iterator().next().getName();
    	
        return UserDTO.builder()
            .id(this.id)
            .nickname(this.nickname)
            .email(this.email)
            .password(this.password)
            .createdAt(this.createdAt)
            .status(this.status)
            .profile(this.profile)
            .roles(roleString)
            .build();
    }
	
    
}
