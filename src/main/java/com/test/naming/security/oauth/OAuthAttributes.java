package com.test.naming.security.oauth;

import java.util.Map;
import java.util.UUID;

import com.test.naming.dto.UserDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        
        // 추후 다른 OAuth 제공자 추가 가능
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider("google")
                .build();
    }

    public UserDTO toUserDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(this.email);
        userDTO.setNickname(this.name);
        userDTO.setProfile(this.picture);
        // 랜덤 비밀번호 생성 (실제로 사용되지는 않음)
        userDTO.setPassword(UUID.randomUUID().toString());
        userDTO.setDefaultValues();
        return userDTO;
    }
}