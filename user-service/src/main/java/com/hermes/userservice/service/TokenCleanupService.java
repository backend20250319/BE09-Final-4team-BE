package com.hermes.userservice.service;

import com.hermes.userservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(fixedRate = 1800000) // 30분 (밀리초)
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            Instant now = Instant.now();
            log.info("만료된 토큰 정리 시작: {}", now);
            
            long expiredCount = refreshTokenRepository.findExpiredTokens(now).size();
            
            if (expiredCount > 0) {
                refreshTokenRepository.deleteExpiredTokens(now);
                log.info("만료된 토큰 {}개 정리 완료", expiredCount);
            } else {
                log.debug("정리할 만료된 토큰이 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("토큰 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void cleanupExpiredTokensByUserId(Long userId) {
        try {
            Instant now = Instant.now();
            refreshTokenRepository.deleteExpiredTokensByUserId(userId, now);
            log.debug("사용자 {}의 만료된 토큰 정리 완료", userId);
        } catch (Exception e) {
            log.error("사용자 {}의 토큰 정리 중 오류 발생: {}", userId, e.getMessage());
        }
    }
}