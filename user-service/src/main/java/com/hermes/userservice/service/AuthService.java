package com.hermes.userservice.service;

import com.hermes.auth.dto.RefreshRequest;
import com.hermes.auth.dto.TokenResponse;
import com.hermes.auth.enums.Role;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.exception.InvalidJwtTokenException;
import com.hermes.userservice.exception.JwtValidationException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 로그인 처리
     */
    public TokenResponse login(LoginRequestDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.updateLastLogin();
        userRepository.save(user);

        Role userRole = getUserRole(user);
        // TODO: tenantId
        String accessToken = jwtTokenService.createAccessToken(user.getId(), user.getEmail(), userRole, null);
        String refreshToken = jwtTokenService.createRefreshToken();

        // 기존 RefreshToken이 있으면 삭제 (이중 로그인 방지)
        refreshTokenRepository.findByUserId(user.getId()).ifPresent(refreshTokenRepository::delete);

        saveRefreshToken(user.getId(), refreshToken);

        log.info("[Auth Service] 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());
        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 로그아웃 처리
     */
    public void logout(Long userId) {
        try {
            // userId로 RefreshToken을 찾아서 삭제
            refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);
            
            // AccessToken을 블랙리스트에 추가하는 것은 의미 없음
            // 각 서비스의 SecurityFilter에서 블랙리스트를 확인하지 않기 때문
            // AccessToken의 수명이 충분히 짧다면 크게 문제되진 않음

        } catch (Exception e) {
            log.error("[Auth Service] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage());
            throw new InvalidJwtTokenException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰 갱신 처리 (Refresh Token Rotation 포함)
     */
    public TokenResponse refreshToken(RefreshRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        RefreshToken saved = refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidJwtTokenException("RefreshToken not found"));

        validateRefreshToken(request.getRefreshToken(), saved, user.getId());
        
        Role userRole = getUserRole(user);
        // TODO: tenantId
        String newAccessToken = jwtTokenService.createAccessToken(user.getId(), request.getEmail(), userRole, null);

        // Refresh Token Rotation: 새로운 RefreshToken 생성
        String newRefreshToken = jwtTokenService.createRefreshToken();
        
        // 기존 RefreshToken 삭제하고 새로운 것으로 교체
        refreshTokenRepository.delete(saved);
        saveRefreshToken(user.getId(), newRefreshToken);

        // 기존 RefreshToken을 블랙리스트에 추가 (보안 강화)
        tokenBlacklistService.addToken(request.getRefreshToken(), jwtTokenService.getRefreshTokenTTL(), user.getId());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    private Role getUserRole(User user) {
        return user.getIsAdmin() ? Role.ADMIN : Role.USER;
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        // RefreshToken을 해시화하여 저장 (보안 강화)
        String hashedRefreshToken = jwtTokenService.hashRefreshToken(refreshToken);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .tokenHash(hashedRefreshToken)
                        .expiration(LocalDateTime.now().plusSeconds(jwtTokenService.getRefreshTokenTTL()))
                        .build()
        );
    }

    private void validateRefreshToken(String refreshToken, RefreshToken saved, Long userId) {
        // 해시된 토큰 검증
        if (!jwtTokenService.verifyRefreshToken(refreshToken, saved.getTokenHash())) {
            log.warn("⚠️ [Auth Service] 유효하지 않은 RefreshToken - userId: {}", userId);
            throw new JwtValidationException("유효하지 않은 RefreshToken입니다.");
        }

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            log.warn("⚠️ [Auth Service] 로그아웃된 RefreshToken - userId: {}", userId);
            throw new JwtValidationException("로그아웃된 Refresh Token입니다.");
        }

        if (saved.isExpired()) {
            log.warn("⚠️ [Auth Service] 만료된 RefreshToken - userId: {}", userId);
            throw new JwtValidationException("만료된 RefreshToken입니다.");
        }
    }
}