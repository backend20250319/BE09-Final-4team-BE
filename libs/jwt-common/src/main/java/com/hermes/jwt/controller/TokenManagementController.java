package com.hermes.jwt.controller;

import com.hermes.jwt.dto.ApiResponse;
import com.hermes.jwt.service.JwtService;
import com.hermes.jwt.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 토큰 관리 컨트롤러 - 로그아웃 및 토큰 관리 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/token-management")
@RequiredArgsConstructor
public class TokenManagementController {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 사용자 로그아웃 처리
     */
    @PostMapping("/logout")
    public ApiResponse<String> logoutUser(
            @RequestParam Long userId,
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String refreshToken) {
        
        try {
            jwtService.logoutUser(userId, accessToken, refreshToken);
            return ApiResponse.success("로그아웃 처리 완료", "사용자 " + userId + "의 모든 토큰이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("로그아웃 처리 실패: userId={}", userId, e);
            return ApiResponse.error("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 로그아웃 처리 (토큰 없이)
     */
    @PostMapping("/logout/{userId}")
    public ApiResponse<String> logoutUserById(@PathVariable Long userId) {
        try {
            jwtService.logoutUser(userId);
            return ApiResponse.success("로그아웃 처리 완료", "사용자 " + userId + "의 모든 토큰이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("로그아웃 처리 실패: userId={}", userId, e);
            return ApiResponse.error("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자의 모든 토큰 삭제
     */
    @DeleteMapping("/tokens/{userId}")
    public ApiResponse<String> deleteAllUserTokens(@PathVariable Long userId) {
        try {
            jwtService.deleteAllUserTokens(userId);
            return ApiResponse.success("토큰 삭제 완료", "사용자 " + userId + "의 모든 토큰이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("토큰 삭제 실패: userId={}", userId, e);
            return ApiResponse.error("토큰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자별 토큰 정보 조회
     */
    @GetMapping("/tokens/{userId}")
    public ApiResponse<TokenBlacklistService.UserTokenInfo> getUserTokenInfo(@PathVariable Long userId) {
        try {
            TokenBlacklistService.UserTokenInfo tokenInfo = jwtService.getUserTokenInfo(userId);
            return ApiResponse.success("토큰 정보 조회 완료", tokenInfo);
        } catch (Exception e) {
            log.error("토큰 정보 조회 실패: userId={}", userId, e);
            return ApiResponse.error("토큰 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 만료된 토큰 정리
     */
    @PostMapping("/cleanup")
    public ApiResponse<String> cleanupExpiredTokens() {
        try {
            jwtService.cleanupExpiredTokens();
            return ApiResponse.success("토큰 정리 완료", "만료된 토큰들이 정리되었습니다.");
        } catch (Exception e) {
            log.error("토큰 정리 실패", e);
            return ApiResponse.error("토큰 정리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 배치 토큰 만료
     */
    @PostMapping("/batch-expire")
    public ApiResponse<String> batchExpireUserTokens(@RequestBody List<Long> userIds) {
        try {
            jwtService.batchExpireUserTokens(userIds);
            return ApiResponse.success("배치 토큰 만료 완료", 
                    userIds.size() + "명의 사용자 토큰이 만료되었습니다.");
        } catch (Exception e) {
            log.error("배치 토큰 만료 실패: userIds={}", userIds, e);
            return ApiResponse.error("배치 토큰 만료 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 배치 토큰 삭제
     */
    @DeleteMapping("/batch-delete")
    public ApiResponse<String> batchDeleteUserTokens(@RequestBody List<Long> userIds) {
        try {
            jwtService.batchDeleteUserTokens(userIds);
            return ApiResponse.success("배치 토큰 삭제 완료", 
                    userIds.size() + "명의 사용자 토큰이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("배치 토큰 삭제 실패: userIds={}", userIds, e);
            return ApiResponse.error("배치 토큰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 토큰 블랙리스트 확인
     */
    @GetMapping("/check-blacklist")
    public ApiResponse<Boolean> checkTokenBlacklist(@RequestParam String token) {
        try {
            boolean isBlacklisted = jwtService.isTokenBlacklisted(token);
            return ApiResponse.success("블랙리스트 확인 완료", isBlacklisted);
        } catch (Exception e) {
            log.error("블랙리스트 확인 실패: token={}", token.substring(0, Math.min(20, token.length())), e);
            return ApiResponse.error("블랙리스트 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 리프레시 토큰 블랙리스트 확인
     */
    @GetMapping("/check-refresh-blacklist")
    public ApiResponse<Boolean> checkRefreshTokenBlacklist(@RequestParam String refreshToken) {
        try {
            boolean isBlacklisted = jwtService.isRefreshTokenBlacklisted(refreshToken);
            return ApiResponse.success("리프레시 토큰 블랙리스트 확인 완료", isBlacklisted);
        } catch (Exception e) {
            log.error("리프레시 토큰 블랙리스트 확인 실패: token={}", 
                    refreshToken.substring(0, Math.min(20, refreshToken.length())), e);
            return ApiResponse.error("리프레시 토큰 블랙리스트 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 블랙리스트 통계 조회
     */
    @GetMapping("/stats")
    public ApiResponse<TokenBlacklistService.BlacklistStats> getBlacklistStats() {
        try {
            TokenBlacklistService.BlacklistStats stats = tokenBlacklistService.getBlacklistStats();
            return ApiResponse.success("블랙리스트 통계 조회 완료", stats);
        } catch (Exception e) {
            log.error("블랙리스트 통계 조회 실패", e);
            return ApiResponse.error("블랙리스트 통계 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
