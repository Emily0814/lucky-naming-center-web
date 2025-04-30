package com.test.naming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;
}