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
@Table(name = "NAME_PREFERENCE")
@SequenceGenerator(
    name = "NAMEPRE_SEQ_GENERATOR",
    sequenceName = "NAMEPRE_SEQ",
    allocationSize = 1
)
public class NamePreference {	//사용자의 이름 선호도 정보를 저장(의미, 분위기, 성별 선호 등)
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NAMEPRE_SEQ_GENERATOR")
    @Column(name = "NAMEPRE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "MEANING", nullable = false, length = 300)
    private String meaning;

    @Column(name = "MOOD", nullable = false, length = 255)
    private String mood;

    @Column(name = "GENDER_PRE", nullable = false, length = 10)
    private String genderPre;

    @Column(name = "NAME_LENGTH", nullable = false, length = 10)
    private String nameLength;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
