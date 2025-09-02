package com.hermes.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "hermes.auth.cookie")
public class AuthCookieProperties {
    
    /**
     * HTTPS에서만 쿠키 전송 여부 (운영환경에서 true)
     */
    private boolean secure = false;
    
    /**
     * SameSite 속성 (CSRF 보호)
     */
    private String sameSite = "Lax";
    
    /**
     * 쿠키 도메인 설정 (운영환경에서 도메인 지정)
     */
    private String domain;
    
    /**
     * RefreshToken 쿠키명
     */
    private String refreshTokenName = "refresh_token";
    
    /**
     * 쿠키 경로
     */
    private String path = "/";
}