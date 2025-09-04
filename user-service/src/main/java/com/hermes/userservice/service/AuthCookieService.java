package com.hermes.userservice.service;

import com.hermes.auth.JwtProperties;
import com.hermes.userservice.config.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthCookieService {
    
    private final AuthCookieProperties authCookieProperties;
    private final JwtProperties jwtProperties;
    
    /**
     * RefreshToken용 ResponseCookie 생성
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(
                authCookieProperties.getRefreshTokenName(), refreshToken)
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .path(authCookieProperties.getPath())
                .maxAge(jwtProperties.getRefreshTokenTTL())
                .sameSite(authCookieProperties.getSameSite());
        
        if (authCookieProperties.getDomain() != null) {
            builder.domain(authCookieProperties.getDomain());
        }
        
        return builder.build();
    }
    
    /**
     * RefreshToken 쿠키 삭제용 ResponseCookie 생성
     */
    public ResponseCookie createRefreshTokenDeleteCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(
                authCookieProperties.getRefreshTokenName(), "")
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .path(authCookieProperties.getPath())
                .maxAge(0)
                .sameSite(authCookieProperties.getSameSite());
        
        if (authCookieProperties.getDomain() != null) {
            builder.domain(authCookieProperties.getDomain());
        }
        
        return builder.build();
    }
    
    /**
     * 요청에서 RefreshToken 쿠키 값 추출
     */
    public Optional<String> getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> authCookieProperties.getRefreshTokenName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}