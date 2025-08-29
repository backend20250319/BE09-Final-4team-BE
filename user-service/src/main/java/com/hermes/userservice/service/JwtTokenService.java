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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    public String createAccessToken(Long userId, String email, Role role, String tenantId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getAccessTokenTTL(), ChronoUnit.SECONDS);

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
     * 리프레시 토큰 생성 (UUID 기반 랜덤 문자열)
     */
    public String createRefreshToken() {
        return java.util.UUID.randomUUID().toString();
    }


    /**
     * 액세스 토큰 만료 시간 반환 (초)
     */
    public long getAccessTokenTTL() {
        return jwtProperties.getAccessTokenTTL();
    }

    /**
     * 리프레시 토큰 만료 시간 반환 (초)
     */
    public long getRefreshTokenTTL() {
        return jwtProperties.getRefreshTokenTTL();
    }

    /**
     * RefreshToken 해시 생성 (SHA-256)
     */
    public String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * RefreshToken 검증 (해시 비교)
     */
    public boolean verifyRefreshToken(String rawToken, String hashedToken) {
        String rawTokenHash = hashRefreshToken(rawToken);
        return rawTokenHash.equals(hashedToken);
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