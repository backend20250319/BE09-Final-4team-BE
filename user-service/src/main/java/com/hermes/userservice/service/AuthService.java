package com.hermes.userservice.service;

import com.hermes.userservice.dto.LoginResult;
import com.hermes.auth.enums.Role;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.exception.InvalidTokenException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResult login(LoginRequestDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.updateLastLogin();
        userRepository.save(user);

        Role userRole = getUserRole(user);
        String accessToken = jwtTokenService.createAccessToken(user.getId(), userRole, null);
        String refreshToken = jwtTokenService.createRefreshToken(user.getId());

        saveOrUpdateRefreshToken(user.getId(), refreshToken);

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

    public void logout(Long userId) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(refreshTokenRepository::delete);
        log.info("[Auth Service] 로그아웃 완료 - userId: {}", userId);
    }

    public LoginResult refreshToken(String refreshToken) {
        Long userId = jwtTokenService.validateAndGetUserIdFromRefreshToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 존재하지 않습니다."));

        validateStoredRefreshToken(userId, refreshToken);

        Role userRole = getUserRole(user);
        String newAccessToken = jwtTokenService.createAccessToken(userId, userRole, null);

        String newRefreshToken = jwtTokenService.createRefreshToken(userId);

        saveOrUpdateRefreshToken(userId, newRefreshToken);

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

    private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        String hashedRefreshToken = jwtTokenService.hashToken(refreshToken);
        Instant expiration = Instant.now().plusSeconds(jwtTokenService.getRefreshTokenTTL());

        RefreshToken existingToken = refreshTokenRepository.findByUserId(userId).orElse(null);
        if (existingToken != null) {
            existingToken.setTokenHash(hashedRefreshToken);
            existingToken.setExpiration(expiration);
            refreshTokenRepository.save(existingToken);
        } else {
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .userId(userId)
                            .tokenHash(hashedRefreshToken)
                            .expiration(expiration)
                            .build()
            );
        }
    }

    private void validateStoredRefreshToken(Long userId, String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidTokenException("RefreshToken not found"));

        if (!jwtTokenService.matchesToken(refreshToken, stored.getTokenHash())) {
            throw new InvalidTokenException("유효하지 않은 RefreshToken입니다.");
        }

        if (stored.isExpired()) {
            throw new InvalidTokenException("만료된 RefreshToken입니다.");
        }
    }
}