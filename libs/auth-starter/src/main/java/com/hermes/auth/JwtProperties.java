package com.hermes.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // 토큰 생성 및 검증에 사용할 Base64 인코딩된 비밀키
    private String secret;

    // 액세스 토큰 만료 시간 (초)
    private long accessTokenTTL;

    // 리프레시 토큰 만료 시간 (초)
    private long refreshTokenTTL;
}
