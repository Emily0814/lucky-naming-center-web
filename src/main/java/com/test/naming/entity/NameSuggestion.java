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
@Table(name = "NAME_SUGGESTION")
@SequenceGenerator(
    name = "NAMESUG_SEQ_GENERATOR",
    sequenceName = "NAMESUG_SEQ",
    allocationSize = 1
)	// 사용자에게 제안된 이름 정보를 저장(이름, 의미, 분위기, 성별 등)
public class NameSuggestion {	//JWT 리프레시 토큰 정보 저장(사용자와 연결된 토큰 정보, 만료 시간 관리)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NAMESUG_SEQ_GENERATOR")
    @Column(name = "NAMESUG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "SUGGESTED_NAME", nullable = false, length = 255)
    private String suggestedName;

    @Column(name = "MEANING", nullable = false, length = 300)
    private String meaning;

    @Column(name = "MOOD", nullable = false, length = 255)
    private String mood;

    @Column(name = "GENDER", nullable = false, length = 10)
    private String gender;

    @Column(name = "IS_FAVORITE", nullable = false, length = 1)
    private String isFavorite;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
