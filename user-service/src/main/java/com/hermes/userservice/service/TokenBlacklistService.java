package com.hermes.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 토큰 블랙리스트 관리 서비스 (user-service 전용)
 * 실제 운영에서는 Redis 등 외부 캐시를 사용하는 것을 권장
 */
@Slf4j
@Service
public class TokenBlacklistService {

    // 메모리 기반 블랙리스트 (실제 운영에서는 Redis 사용 권장)
    private final ConcurrentMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * 사용자 로그아웃 - 토큰들을 블랙리스트에 추가
     */
    public void logoutUser(Long userId, String accessToken, String refreshToken) {
        log.info("사용자 로그아웃 처리: userId={}", userId);
        
        if (accessToken != null && !accessToken.isEmpty()) {
            blacklistedTokens.put(accessToken, userId);
            log.debug("Access Token을 블랙리스트에 추가: userId={}", userId);
        }
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            blacklistedTokens.put(refreshToken, userId);
            log.debug("Refresh Token을 블랙리스트에 추가: userId={}", userId);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * 블랙리스트 통계 정보 (모니터링용)
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * 블랙리스트 초기화 (테스트용 - 운영 사용 금지)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
        log.warn("토큰 블랙리스트가 초기화되었습니다.");
    }
}