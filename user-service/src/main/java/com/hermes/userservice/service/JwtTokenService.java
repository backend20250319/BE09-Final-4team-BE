package com.hermes.userservice.service;

import com.hermes.auth.JwtProperties;
import com.hermes.auth.context.Role;
import com.hermes.auth.principal.UserPrincipal;
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
     * 토큰에서 사용자 정보 추출 (로그아웃 처리용)
     */
    public UserPrincipal getUserFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            Object userIdObj = claims.get("userId");
            Long userId = userIdObj instanceof Integer 
                ? ((Integer) userIdObj).longValue() 
                : (Long) userIdObj;
            
            String roleStr = (String) claims.get("role");
            Role role = Role.fromString(roleStr);
            String tenantId = (String) claims.get("tenantId");

            return new UserPrincipal(userId, email, role, tenantId);
            
        } catch (JwtException e) {
            log.warn("토큰 파싱 실패: {}", e.getMessage());
            return null;
        }
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
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}