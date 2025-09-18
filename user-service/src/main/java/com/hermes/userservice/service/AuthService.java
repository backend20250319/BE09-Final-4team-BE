package com.hermes.userservice.service;

import com.hermes.multitenancy.context.TenantContext;
import com.hermes.userservice.dto.LoginResult;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.dto.PasswordChangeRequestDto;
import com.hermes.userservice.dto.RefreshTokenInfo;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.entity.UserTenant;
import com.hermes.userservice.exception.InvalidTokenException;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 멀티테넌트 인증 오케스트레이션 서비스
 *
 * 주의: 이 클래스에는 @Transactional을 사용하면 안됩니다.
 * executeWithTenant() 호출 전에 트랜잭션이 시작되면 TenantIdentifierResolver가
 * 잘못된 테넌트 컨텍스트에서 실행되어 멀티테넌시가 제대로 작동하지 않습니다.
 *
 * 실제 트랜잭션은 TenantAuthService에서 테넌트 컨텍스트 설정 후에 시작됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantAuthService tenantAuthService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserTenantRepository userTenantRepository;

    /**
     * 로그인 처리
     */
    public LoginResult login(LoginRequestDto loginDto) {
        // 1. tenantId 조회 (NonTenant 컨텍스트에서 실행)
        String tenantId = getTenantId(loginDto.getEmail());
        if (tenantId == null) {
            throw new UserNotFoundException("해당 이메일로 등록된 테넌트가 없습니다.");
        }

        // 2. Tenant 컨텍스트에서 사용자 인증
        return TenantContext.executeWithTenant(tenantId, () -> {
            return tenantAuthService.authenticateUser(loginDto, tenantId);
        });
    }

    /**
     * 로그아웃 처리
     */
    public void logout(Long userId) {
        if (!TenantContext.hasTenantContext()) {
            throw new IllegalStateException("테넌트 컨텍스트가 설정되지 않았습니다. logout은 테넌트 컨텍스트에서 호출되어야 합니다.");
        }
        tenantAuthService.logoutUser(userId);
    }

    /**
     * 비밀번호 변경 처리
     */
    public void changePassword(Long userId, PasswordChangeRequestDto passwordChangeDto) {
        if (!TenantContext.hasTenantContext()) {
            throw new IllegalStateException("테넌트 컨텍스트가 설정되지 않았습니다. changePassword는 테넌트 컨텍스트에서 호출되어야 합니다.");
        }
        tenantAuthService.changeUserPassword(userId, passwordChangeDto);
    }

    /**
     * 토큰 갱신 처리 (Refresh Token Rotation 포함)
     */
    public LoginResult refreshToken(String refreshToken) {
        RefreshTokenInfo tokenInfo = jwtTokenService.validateRefreshTokenAndExtractInfo(refreshToken);
        Long userId = tokenInfo.getUserId();
        String tenantId = tokenInfo.getTenantId();

        if (tenantId == null) {
            throw new UserNotFoundException("RefreshToken에 테넌트 정보가 없습니다.");
        }

        // RefreshToken 검증 (NonTenant 컨텍스트에서 수행)
        validateStoredRefreshToken(userId, refreshToken);

        // Tenant 컨텍스트에서 토큰 갱신
        return TenantContext.executeWithTenant(tenantId, () -> {
            return tenantAuthService.renewToken(userId, tenantId);
        });
    }

    private String getTenantId(String email) {
        return userTenantRepository.findByEmail(email)
                .map(UserTenant::getTenantId)
                .orElse(null);
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
