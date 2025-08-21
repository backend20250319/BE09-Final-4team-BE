package com.hermes.jwt;

import com.hermes.jwt.context.Role;
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

    public boolean isValidToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (InvalidJwtException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            return parseToken(token).getPayload().getSubject();
        } catch (InvalidJwtException e) {
            return null;
        }
    }

    public JwtPayload getPayloadFromToken(String token) {
        Claims claims = parseToken(token).getPayload();

        String email = claims.getSubject();
        Object userIdObj = claims.get("userId");
        Object roleObj = claims.get("role");

        String userId = userIdObj != null ? userIdObj.toString() : null;
        String role = roleObj != null ? roleObj.toString() : null;

        return new JwtPayload(userId, email, role);
    }
    
    /**
     * 토큰에서 Role enum을 추출합니다.
     */
    public Role getRoleFromToken(String token) {
        String roleString = getClaimFromToken(token, "role");
        return Role.fromString(roleString, Role.USER);
    }

    public String getClaimFromToken(String token, String claimName) {
        Claims claims = parseToken(token).getPayload();
        Object value = claims.get(claimName);
        return value != null ? value.toString() : null;
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
