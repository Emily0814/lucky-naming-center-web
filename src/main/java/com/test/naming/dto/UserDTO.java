package com.test.naming.dto;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.test.naming.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor	//Lombok의 어노테이션이 작동하기 위해서는 모든 필드를 매개변수로 받는 생성자가 필요하기 때문에 필요함 
@NoArgsConstructor	//기본 생성자 추가
@Builder	
public class UserDTO {

	private Long id;
	//회원가입 필수값
    private String email;
    private String password;
    private LocalDateTime createdAt;
    //회원가입 선택값
    private String nickname;
    private String profile;
	
	//서버에서 자동 설정되는 값
    private Integer status;      
    private String roles;
    
    @JsonIgnore
    private MultipartFile profileFile;
	
	//기본값을 설정하는 메서드
    public void setDefaultValues() {
        this.createdAt = LocalDateTime.now();
        this.status = 1;	//1:활성 상태, 0:비활성 상태
        this.roles = "ROLE_USER";
    }
	
    //toEntity 메서드는 더 이상 필요하지 않을 수 있음(UserMapper가 있으으로)
    //유지하려면 다음과 같이 수정
    public User toEntity() {
        return User.builder()
                .id(this.getId())
                .email(this.getEmail())
                .password(this.getPassword())
                .nickname(this.getNickname())
                .profile(this.getProfile())
                .createdAt(this.getCreatedAt())
                .status(this.getStatus())
                //roles는 매퍼에서 처리
                .build();
    }

}
