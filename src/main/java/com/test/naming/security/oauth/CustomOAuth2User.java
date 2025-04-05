package com.test.naming.security.oauth;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import lombok.Getter;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private String email;
    private String provider;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                           Map<String, Object> attributes, 
                           String nameAttributeKey,
                           String email,
                           String provider) {
        super(authorities, attributes, nameAttributeKey);
        this.email = email;
        this.provider = provider;
    }
}