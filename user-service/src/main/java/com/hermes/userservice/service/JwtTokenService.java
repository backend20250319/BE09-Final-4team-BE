package com.hermes.userservice.service;

import com.hermes.auth.JwtProperties;
import com.hermes.auth.enums.Role;
import com.hermes.userservice.exception.InvalidTokenException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public String createAccessToken(Long userId, Role role, String tenantId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(getAccessTokenTTL(), ChronoUnit.SECONDS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role.name());
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }

        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(getRefreshTokenTTL(), ChronoUnit.SECONDS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public long getAccessTokenTTL() {
        return jwtProperties.getAccessTokenTTL();
    }

    public long getRefreshTokenTTL() {
        return jwtProperties.getRefreshTokenTTL();
    }

    public String hashToken(String token) {
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

    public boolean matchesToken(String rawToken, String hashedToken) {
        String rawTokenHash = hashToken(rawToken);
        return rawTokenHash.equals(hashedToken);
    }

    public Long validateAndGetUserIdFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                throw new InvalidTokenException("리프레시 토큰이 아닙니다.");
            }

            return claims.get("userId", Long.class);

        } catch (JwtException e) {
            throw new InvalidTokenException("유효하지 않은 토큰", e);
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}