package com.hermes.auth.service;

import com.hermes.auth.JwtTokenProvider;
import com.hermes.auth.context.Role;
import com.hermes.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Access Token과 Refresh Token을 생성합니다.
     * 
     * @param email 사용자 이메일
     * @param userId 사용자 ID
     * @param role 사용자 역할
     * @return TokenResponse (accessToken, refreshToken)
     */
    public TokenResponse createTokens(String email, Long userId, String role) {
        String accessToken = jwtTokenProvider.createToken(email, userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(userId), email);
        
        return new TokenResponse(accessToken, refreshToken);
    }
    
    /**
     * Access Token과 Refresh Token을 생성합니다. (Role enum 사용)
     * 
     * @param email 사용자 이메일
     * @param userId 사용자 ID
     * @param role 사용자 역할 (Role enum)
     * @return TokenResponse (accessToken, refreshToken)
     */
    public TokenResponse createTokens(String email, Long userId, Role role) {
        String accessToken = jwtTokenProvider.createToken(email, userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(userId), email);
        
        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * JWT 토큰에서 사용자 정보를 추출합니다.
     * JwtTokenProvider.getUserInfoFromToken을 직접 사용하는 것을 권장합니다.
     * 
     * @param token JWT 토큰
     * @return 사용자 정보
     * @deprecated JwtTokenProvider.getUserInfoFromToken을 직접 사용하세요
     */
    @Deprecated
    public boolean validateToken(String token) {
        try {
            jwtTokenProvider.getUserInfoFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰의 만료 시간을 계산합니다.
     * 
     * @return 만료 시간 (LocalDateTime)
     */
    public LocalDateTime calculateTokenExpiration() {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtTokenProvider.getExpirationTime());
        return expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Refresh Token의 만료 시간을 계산합니다.
     * 
     * @return 만료 시간 (LocalDateTime)
     */
    public LocalDateTime calculateRefreshTokenExpiration() {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration());
        return expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // ========== 로그아웃 및 토큰 관리 메서드들 ==========

    /**
     * 사용자 로그아웃 처리 - 모든 토큰을 완전히 삭제
     * 
     * @param userId 로그아웃할 사용자 ID
     * @param accessToken 현재 액세스 토큰 (선택사항)
     * @param refreshToken 현재 리프레시 토큰 (선택사항)
     */
    public void logoutUser(Long userId, String accessToken, String refreshToken) {
        tokenBlacklistService.logoutUser(userId, accessToken, refreshToken);
    }

    /**
     * 사용자 로그아웃 처리 (토큰 없이)
     * 
     * @param userId 로그아웃할 사용자 ID
     */
    public void logoutUser(Long userId) {
        logoutUser(userId, null, null);
    }

    /**
     * 사용자의 모든 토큰을 완전히 삭제
     * 
     * @param userId 삭제할 사용자 ID
     */
    public void deleteAllUserTokens(Long userId) {
        tokenBlacklistService.deleteAllUserTokens(userId);
    }

    /**
     * 만료된 토큰들을 완전히 삭제 (메모리 정리)
     */
    public void cleanupExpiredTokens() {
        tokenBlacklistService.deleteExpiredTokens();
    }

    /**
     * 사용자별 토큰 정보 조회
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 토큰 정보
     */
    public TokenBlacklistService.UserTokenInfo getUserTokenInfo(Long userId) {
        return tokenBlacklistService.getUserTokenInfo(userId);
    }

    /**
     * 배치로 여러 사용자의 토큰을 만료
     * 
     * @param userIds 만료할 사용자 ID 목록
     */
    public void batchExpireUserTokens(List<Long> userIds) {
        tokenBlacklistService.batchExpireUserTokens(userIds);
    }

    /**
     * 배치로 여러 사용자의 토큰을 삭제
     * 
     * @param userIds 삭제할 사용자 ID 목록
     */
    public void batchDeleteUserTokens(List<Long> userIds) {
        tokenBlacklistService.batchDeleteUserTokens(userIds);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * @param token 확인할 토큰
     * @return 블랙리스트 여부
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.isBlacklisted(token);
    }

    /**
     * 리프레시 토큰이 블랙리스트에 있는지 확인
     * 
     * @param refreshToken 확인할 리프레시 토큰
     * @return 블랙리스트 여부
     */
    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        return tokenBlacklistService.isRefreshTokenBlacklisted(refreshToken);
    }
}