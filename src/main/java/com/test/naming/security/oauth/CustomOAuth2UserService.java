package com.test.naming.security.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.test.naming.entity.User;
import com.test.naming.repository.UserRepository;
import com.test.naming.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 서비스 구분 (google, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 로그인 시도: {}", registrationId);
        
        // OAuth2 로그인 진행 시 키가 되는 필드값 (PK)
        String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        
        // OAuth2User에서 사용자 정보 추출
        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId, 
                userNameAttributeName, 
                oAuth2User.getAttributes()
        );
        
        // 사용자 저장 또는 업데이트
        User user = saveOrUpdate(attributes);
        
        return new CustomOAuth2User(
                oAuth2User.getAuthorities(),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                user.getEmail(),
                registrationId
        );
    }
    
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail());
        
        if (user == null) {
            // 신규 사용자는 회원가입 처리
            return userService.registerOAuth2User(attributes.toUserDTO());
        }
        
        // 기존 사용자는 정보 업데이트 (필요시)
        return user;
    }
}