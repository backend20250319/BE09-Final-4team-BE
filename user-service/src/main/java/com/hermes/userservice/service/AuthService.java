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
        log.info("[Auth Service] 로그인 처리 시작 - email: {}", loginDto.getEmail());
        
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.updateLastLogin();
        userRepository.save(user);

        Role userRole = getUserRole(user);
        String accessToken = jwtTokenService.createAccessToken(user.getEmail(), user.getId(), userRole, null);
        String refreshToken = jwtTokenService.createRefreshToken(String.valueOf(user.getId()), user.getEmail());

        // 기존 RefreshToken이 있으면 삭제 (이중 로그인 방지)
        refreshTokenRepository.findByUserId(user.getId()).ifPresent(refreshTokenRepository::delete);

        saveRefreshToken(user.getId(), refreshToken);

        log.info("[Auth Service] 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());
        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 로그아웃 처리
     */
    public void logout(Long userId, String accessToken, String refreshTokenHash) {
        log.info("[Auth Service] 로그아웃 처리 시작 - userId: {}", userId);

        try {
            // userId로 RefreshToken을 찾아서 삭제
            refreshTokenRepository.findByUserId(userId).ifPresent(rt -> {
                refreshTokenRepository.delete(rt);
                log.info("[Auth Service] RefreshToken 삭제 완료 - userId: {}", userId);
            });
            
            // AccessToken을 블랙리스트에 추가 (보안 강화)
            if (accessToken != null) {
                tokenBlacklistService.addToken(accessToken, jwtTokenService.getAccessTokenExpirySeconds(), userId);
            }
            log.info("[Auth Service] 모든 토큰 완전 삭제 완료 (블랙리스트 포함) - userId: {}", userId);
        } catch (Exception e) {
            log.error("[Auth Service] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new InvalidJwtTokenException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰 갱신 처리 (Refresh Token Rotation 포함)
     */
    public TokenResponse refreshToken(Long userId, String email, RefreshRequest request) {
        log.info("[Auth Service] 토큰 갱신 처리 시작 - userId: {}", userId);

        RefreshToken saved = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidJwtTokenException("RefreshToken not found"));

        validateRefreshToken(request.getRefreshToken(), saved, userId);

        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Role userRole = getUserRole(userEntity);
        String newAccessToken = jwtTokenService.createAccessToken(email, userId, userRole, null);

        // Refresh Token Rotation: 새로운 RefreshToken 생성
        String newRefreshToken = jwtTokenService.createRefreshToken(String.valueOf(userId), email);
        
        // 기존 RefreshToken 삭제하고 새로운 것으로 교체
        refreshTokenRepository.delete(saved);
        saveRefreshToken(userId, newRefreshToken);

        // 기존 RefreshToken을 블랙리스트에 추가 (보안 강화)
        tokenBlacklistService.addToken(request.getRefreshToken(), jwtTokenService.getRefreshTokenExpirySeconds(), userId);

        log.info("[Auth Service] 토큰 갱신 성공 (Token Rotation 적용) - userId: {}", userId);
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
                        .expiration(LocalDateTime.now().plusSeconds(jwtTokenService.getRefreshTokenExpirySeconds()))
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