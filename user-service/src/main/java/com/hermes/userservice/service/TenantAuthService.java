package com.hermes.userservice.service;

import com.hermes.userservice.dto.LoginResult;
import com.hermes.auth.enums.Role;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.dto.PasswordChangeRequestDto;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.exception.InvalidCredentialsException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 테넌트 컨텍스트에서 실행되는 사용자 인증 및 관리 서비스
 * 모든 테넌트별 사용자 데이터 조작을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TenantAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    /**
     * 테넌트 컨텍스트에서 사용자 인증
     */
    public LoginResult authenticateUser(LoginRequestDto loginDto, String tenantId) {
        // 현재 search_path 확인 및 로깅
        logCurrentSearchPath(tenantId);

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 등록된 사용자가 없습니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        user.updateLastLogin();
        userRepository.save(user);

        Role userRole = getUserRole(user);
        String accessToken = jwtTokenService.createAccessToken(user.getId(), userRole, tenantId);
        String refreshToken = jwtTokenService.createRefreshToken(user.getId(), tenantId);

        // 기존 RefreshToken이 있으면 업데이트, 없으면 새로 생성 (이중 로그인 방지)
        // 추후 다중 로그인을 지원하려면 device_id 같은 정보를 추가하여 여러 개의 Refresh Token을 관리할 수 있도록 개선 필요
        saveOrUpdateRefreshToken(user.getId(), refreshToken);

        log.info("[Tenant Auth Service] 로그인 성공 - userId: {}, email: {}, needsPasswordReset: {}, tenantId: {}",
                user.getId(), user.getEmail(), user.getNeedsPasswordReset(), tenantId);

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
     * 테넌트 컨텍스트에서 토큰 갱신
     */
    public LoginResult renewToken(Long userId, String tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 존재하지 않습니다."));

        Role userRole = getUserRole(user);
        String newAccessToken = jwtTokenService.createAccessToken(userId, userRole, tenantId);

        // Refresh Token Rotation: 새로운 RefreshToken 생성
        String newRefreshToken = jwtTokenService.createRefreshToken(userId, tenantId);

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

    /**
     * 테넌트 컨텍스트에서 비밀번호 변경
     */
    public void changeUserPassword(Long userId, PasswordChangeRequestDto passwordChangeDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 존재하지 않습니다."));

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);

        log.info("[Tenant Auth Service] 비밀번호 변경 완료 - userId: {}", userId);
    }

    /**
     * 테넌트 컨텍스트에서 로그아웃 처리
     */
    public void logoutUser(Long userId) {
        // userId로 RefreshToken을 찾아서 삭제
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(refreshTokenRepository::delete);

        // Token Blacklist는 삭제함
        // 매 요청마다 블랙리스트를 확인해야 하는데, 성능에 안좋기 때문
        // 대신 Access Token의 TTL을 짧게 설정하는 것으로 어느정도 대응 가능

        log.info("[Tenant Auth Service] 로그아웃 완료 - userId: {}", userId);
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

    /**
     * 현재 데이터베이스 search_path 확인 및 로깅
     */
    private void logCurrentSearchPath(String expectedTenantId) {
        try {
            Query query = entityManager.createNativeQuery("SHOW search_path");
            String currentSearchPath = (String) query.getSingleResult();

            log.info("[Tenant Auth Service] 현재 DB search_path: '{}', 예상 테넌트: '{}', 스레드: {}",
                    currentSearchPath, expectedTenantId, Thread.currentThread().getName());

            // 예상 테넌트 스키마가 search_path에 포함되어 있는지 확인
            String expectedSchema = "tenant_" + expectedTenantId;
            if (!currentSearchPath.contains(expectedSchema)) {
                log.warn("[Tenant Auth Service] 경고: 예상 스키마 '{}'가 search_path '{}'에 포함되지 않음!",
                        expectedSchema, currentSearchPath);
            }
        } catch (Exception e) {
            log.error("[Tenant Auth Service] search_path 조회 중 오류 발생", e);
        }
    }
}