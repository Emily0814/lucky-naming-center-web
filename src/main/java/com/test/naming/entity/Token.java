package com.test.naming.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "TOKEN")
@SequenceGenerator(
	name = "TOKEN_SEQ_GENERATOR",
    sequenceName = "TOKEN_SEQ",
    allocationSize = 1
)
public class Token {	//인증 토큰 정보를 저장(액세스 토큰, 리프레시 토큰 등)
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TOKEN_SEQ_GENERATOR")
	@Column(name = "TOKEN_ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;

	@Column(name = "ACCESS_TOKEN", nullable = false, length = 500)
	private String accessToken;
	
	@Column(name = "REFRESH_TOKEN", length = 500)
	private String refreshToken;
	
	@Column(name = "EXPIRES_AT", nullable = false)
	private LocalDateTime expiresAt;
	
	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;
	
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
	
}

















