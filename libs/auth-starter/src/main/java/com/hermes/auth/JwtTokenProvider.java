package com.hermes.auth;

import com.hermes.auth.context.Role;
import com.hermes.auth.context.UserInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtTokenProvider {

    private final SecretKey secretKey;

    @Getter
    private final long expirationTime;

    @Getter
    private final long refreshExpiration;

    public JwtTokenProvider(JwtProperties properties) {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = properties.getExpirationTime();
        this.refreshExpiration = properties.getRefreshExpiration();
    }

    public String createToken(String email, Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
    
    public String createToken(String email, Long userId, Role role) {
        String roleString = role != null ? role.name() : Role.USER.name();
        return createToken(email, userId, roleString);
    }

    public String createRefreshToken(String subject,  String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }


    /**
     * JWT 토큰에서 사용자 정보를 추출합니다.
     * 토큰이 유효하지 않으면 InvalidJwtException을 던집니다.
     * 
     * @param token JWT 토큰
     * @return 사용자 정보
     * @throws InvalidJwtException 토큰이 유효하지 않은 경우
     */
    public UserInfo getUserInfoFromToken(String token) {
        Claims claims = parseToken(token).getPayload();

        String email = claims.getSubject();
        Object userIdObj = claims.get("userId");
        Object roleObj = claims.get("role");
        Object tenantIdObj = claims.get("tenantId");

        Long userId = null;
        if (userIdObj != null) {
            try {
                userId = Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                throw new InvalidJwtException("Invalid userId format in JWT token: " + userIdObj);
            }
        }
        
        Role role = Role.fromString(roleObj != null ? roleObj.toString() : null, Role.USER);
        String tenantId = tenantIdObj != null ? tenantIdObj.toString() : null;

        return UserInfo.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .tenantId(tenantId)
                .build();
    }
    

    private Jws<Claims> parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException e) {
            throw new InvalidJwtException("Invalid JWT Token");
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtException("Expired JWT Token");
        } catch (UnsupportedJwtException e) {
            throw new InvalidJwtException("Unsupported JWT Token");
        } catch (IllegalArgumentException e) {
            throw new InvalidJwtException("JWT Token claims empty");
        }
    }
}
