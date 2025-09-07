package com.hermes.userservice.service;

import com.hermes.userservice.dto.LoginResult;
import com.hermes.auth.enums.Role;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.dto.PasswordChangeRequestDto;
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
        String accessToken = jwtTokenService.createAccessToken(user.getId(), userRole, null);
        String refreshToken = jwtTokenService.createRefreshToken(user.getId());

        // 기존 RefreshToken이 있으면 업데이트, 없으면 새로 생성 (이중 로그인 방지)
        // 추후 다중 로그인을 지원하려면 device_id 같은 정보를 추가하여 여러 개의 Refresh Token을 관리할 수 있도록 개선 필요
        saveOrUpdateRefreshToken(user.getId(), refreshToken);

        log.info("[Auth Service] 로그인 성공 - userId: {}, email: {}, needsPasswordReset: {}", user.getId(), user.getEmail(), user.getNeedsPasswordReset());
        return LoginResult.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .expiresIn(jwtTokenService.getAccessTokenTTL())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(userRole.name())
                .needsPasswordReset(user.getNeedsPasswordReset())
                .build();
    }

    /**
     * 로그아웃 처리
     */
    public void logout(Long userId) {
        // userId로 RefreshToken을 찾아서 삭제
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(refreshTokenRepository::delete);

        // Token Blacklist는 삭제함
        // 매 요청마다 블랙리스트를 확인해야 하는데, 성능에 안좋기 때문
        // 대신 Access Token의 TTL을 짧게 설정하는 것으로 어느정도 대응 가능

        log.info("[Auth Service] 로그아웃 완료 - userId: {}", userId);
    }

    /**
     * 비밀번호 변경 처리
     */
    public void changePassword(Long userId, PasswordChangeRequestDto passwordChangeDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 존재하지 않습니다."));

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        user.updatePassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);

        log.info("[Auth Service] 비밀번호 변경 완료 - userId: {}", userId);
    }

    /**
     * 토큰 갱신 처리 (Refresh Token Rotation 포함)
     */
    public LoginResult refreshToken(String refreshToken) {
        // refreshToken을 검증하고 userId 추출
        Long userId = jwtTokenService.validateAndGetUserIdFromRefreshToken(refreshToken);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 존재하지 않습니다."));

        validateStoredRefreshToken(userId, refreshToken);
        
        Role userRole = getUserRole(user);
        // TODO: tenantId
        String newAccessToken = jwtTokenService.createAccessToken(userId, userRole, null);

        // Refresh Token Rotation: 새로운 RefreshToken 생성
        String newRefreshToken = jwtTokenService.createRefreshToken(userId);
        
        // 기존 RefreshToken을 새로운 것으로 교체
        saveOrUpdateRefreshToken(userId, newRefreshToken);

        return LoginResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(userRole.name())
                .needsPasswordReset(user.getNeedsPasswordReset())
                .expiresIn(jwtTokenService.getAccessTokenTTL())
                .build();
    }

    private Role getUserRole(User user) {
        return user.getIsAdmin() ? Role.ADMIN : Role.USER;
    }

    private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        // RefreshToken을 해시화하여 저장 (보안 강화)
        String hashedRefreshToken = jwtTokenService.hashToken(refreshToken);
        Instant expiration = Instant.now().plusSeconds(jwtTokenService.getRefreshTokenTTL());
        
        // 기존 토큰이 있으면 업데이트, 없으면 새로 생성 (upsert)
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

        // 토큰 해시값 비교
        if (!jwtTokenService.matchesToken(refreshToken, stored.getTokenHash())) {
            throw new InvalidTokenException("유효하지 않은 RefreshToken입니다.");
        }

        // DB 만료시간 확인 (추가 보안)
        if (stored.isExpired()) {
            throw new InvalidTokenException("만료된 RefreshToken입니다.");
        }
    }
}
