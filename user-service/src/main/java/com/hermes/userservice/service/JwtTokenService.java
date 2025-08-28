package com.hermes.userservice.service;

import com.hermes.auth.JwtProperties;
import com.hermes.auth.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.io.Decoders;

/**
 * JWT 토큰 생성 전용 서비스 (user-service에서만 사용)
 * 토큰 검증은 Spring Security OAuth2 Resource Server가 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    /**
     * 액세스 토큰 생성
     */
    public String createAccessToken(String email, Long userId, Role role, String tenantId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getExpirationTime(), ChronoUnit.MILLIS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role.name());
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     */
    public String createRefreshToken(String userId, String email) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getRefreshExpiration(), ChronoUnit.MILLIS);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }


    /**
     * 액세스 토큰 만료 시간 반환
     */
    public long getAccessTokenExpiration() {
        return jwtProperties.getExpirationTime();
    }

    /**
     * 리프레시 토큰 만료 시간 반환
     */
    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshExpiration();
    }

    /**
     * JWT 서명 키 생성
     */
    private SecretKey getSigningKey() {
        // Base64 디코딩 후 사용
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}