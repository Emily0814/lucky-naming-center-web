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
@Table(name = "APPLICATION_LOG")
@SequenceGenerator(
    name = "LOG_SEQ_GENERATOR",
    sequenceName = "LOG_SEQ",
    allocationSize = 1
)
public class ApplicationLog {	// 애플리케이션 사용 로그를 저장(사용자 활동, 시간 등)
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_SEQ_GENERATOR")
    @Column(name = "LOG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "ACTION", nullable = false, length = 100)
    private String action;

    @Column(name = "TIMESTAMP", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
