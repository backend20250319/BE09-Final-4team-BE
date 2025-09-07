package com.hermes.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResult {
    private String refreshToken;
    private String accessToken;
    private Long expiresIn;
    private Long userId;
    private String email;
    private String name;
    private String role;
    private Boolean needsPasswordReset;
}
