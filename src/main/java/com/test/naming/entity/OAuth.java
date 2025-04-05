package com.test.naming.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "OAUTH")
@SequenceGenerator(
	name = "OAUTH_SEQ_GENERATOR",
	sequenceName = "OAUTH_SEQ",
	allocationSize = 1
)
public class OAuth {	//소셜 로그인 사용자 정보를 저장(제공자(Google, Kakao 등) 정보, 소셜 ID와 기타 프로필 정보)

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OAUTH_SEQ_GENERATOR")
	@Column(name = "OAUTH_ID")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;
	
	@Column(name = "OAUTH_EMAIL", nullable = false, length = 300)
	private String oauthEmail;
	
	@Column(name = "PROVIDER", nullable = false, length = 10)
	private String provider;
	
	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "UPDATE_AT", nullable = false)
	private LocalDateTime updateAt;
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updateAt = LocalDateTime.now();
	}
	
	protected void onUpdate() {
		updateAt = LocalDateTime.now();
	}
		
}







