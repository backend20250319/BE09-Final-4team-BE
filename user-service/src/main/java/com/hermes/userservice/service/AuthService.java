package com.hermes.userservice.service;

import com.hermes.userservice.dto.LoginResult;
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

    /**
     * 로그인 처리
     */
    public LoginResult login(LoginRequestDto loginDto) {
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
        // 추후 다중 로그인을 지원하려면 device_id 같은 정보를 추가하여 여러 개의 Refresh Token을 관리할 수 있도록 개선 필요
        refreshTokenRepository.findByUserId(user.getId()).ifPresent(entity -> {
            refreshTokenRepository.delete(entity);
            refreshTokenRepository.flush();
        });

        saveRefreshToken(user.getId(), refreshToken);

        log.info("[Auth Service] 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());
        return LoginResult.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .expiresIn(jwtTokenService.getAccessTokenTTL())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(userRole.name())
                .build();
    }

    /**
     * 로그아웃 처리
     */
    public void logout(Long userId) {
        try {
            // userId로 RefreshToken을 찾아서 삭제
            refreshTokenRepository.findByUserId(userId)
                    .ifPresent(refreshTokenRepository::delete);

            // Token Blacklist는 삭제함
            // 매 요청마다 블랙리스트를 확인해야 하는데, 성능에 안좋기 때문
            // 대신 Access Token의 TTL을 짧게 설정하는 것으로 어느정도 대응 가능
            
            log.info("[Auth Service] 로그아웃 완료 - userId: {}", userId);

        } catch (Exception e) {
            log.error("[Auth Service] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage());
            throw new InvalidJwtTokenException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰 갱신 처리 (Refresh Token Rotation 포함)
     */
    public LoginResult refreshToken(String email, String refreshToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        RefreshToken saved = refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidJwtTokenException("RefreshToken not found"));

        validateRefreshToken(refreshToken, saved, user.getId());
        
        Role userRole = getUserRole(user);
        // TODO: tenantId
        String newAccessToken = jwtTokenService.createAccessToken(user.getId(), email, userRole, null);

        // Refresh Token Rotation: 새로운 RefreshToken 생성
        String newRefreshToken = jwtTokenService.createRefreshToken();
        
        // 기존 RefreshToken 삭제하고 새로운 것으로 교체
        refreshTokenRepository.delete(saved);
        refreshTokenRepository.flush();
        saveRefreshToken(user.getId(), newRefreshToken);

        return LoginResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(userRole.name())
                .expiresIn(jwtTokenService.getAccessTokenTTL())
                .build();
    }

    private Role getUserRole(User user) {
        return user.getIsAdmin() ? Role.ADMIN : Role.USER;
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        // RefreshToken을 해시화하여 저장 (보안 강화)
        String hashedRefreshToken = jwtTokenService.hashToken(refreshToken);
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
        if (!jwtTokenService.verifyToken(refreshToken, saved.getTokenHash())) {
            log.warn("⚠️ [Auth Service] 유효하지 않은 RefreshToken - userId: {}", userId);
            throw new JwtValidationException("유효하지 않은 RefreshToken입니다.");
        }

        if (saved.isExpired()) {
            log.warn("⚠️ [Auth Service] 만료된 RefreshToken - userId: {}", userId);
            throw new JwtValidationException("만료된 RefreshToken입니다.");
        }
    }
}
