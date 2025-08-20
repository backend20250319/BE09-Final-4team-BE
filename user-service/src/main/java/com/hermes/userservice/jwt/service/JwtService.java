package com.hermes.userservice.jwt.service;

import com.hermes.jwt.JwtProperties;
import com.hermes.userservice.jwt.dto.RefreshRequest;
import com.hermes.userservice.jwt.dto.TokenRequest;
import com.hermes.userservice.jwt.dto.TokenResponse;
import com.hermes.userservice.jwt.entity.RefreshToken;
import com.hermes.userservice.jwt.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] secretBytes = java.util.Base64.getDecoder().decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpirationTime());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiration(expiryDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public TokenResponse createToken(TokenRequest request) {
        String accessToken = generateAccessToken(request.getUserId(), request.getEmail());
        String refreshToken = generateRefreshToken(request.getUserId());
        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshToken(RefreshRequest request) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByUserId(request.getUserId());

        if (refreshTokenOpt.isEmpty()) {
            throw new RuntimeException("Refresh token not found for user.");
        }

        RefreshToken savedToken = refreshTokenOpt.get();

        if (!savedToken.getToken().equals(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token.");
        }

        String newAccessToken = generateAccessToken(request.getUserId(), request.getEmail());
        String newRefreshToken = generateRefreshToken(request.getUserId());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
