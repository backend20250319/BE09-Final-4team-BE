package com.hermes.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


// User Service 전용 토큰 블랙리스트 서비스 로그아웃된 Access Token과 Refresh Token을 관리하고 검증하는 서비스

@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blacklistedRefreshTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> userLogoutTimes = new ConcurrentHashMap<>();
    
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_BLACKLIST_PREFIX = "jwt:refresh:blacklist:";
    private static final String LOGOUT_PREFIX = "jwt:logout:";

    public void blacklistToken(String token, long expirationTimeMillis) {
        try {
            String key = BLACKLIST_PREFIX + generateTokenHash(token);
            long expirationTime = System.currentTimeMillis() + expirationTimeMillis;
            
            blacklistedTokens.put(key, expirationTime);
            log.info(" [TokenBlacklistService] Access Token 블랙리스트 추가 완료: {}",
                    key.substring(0, Math.min(20, key.length())) + "...");
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] Access Token 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }

    public void blacklistRefreshToken(String refreshToken, long expirationTimeMillis) {
        try {
            String key = REFRESH_BLACKLIST_PREFIX + generateTokenHash(refreshToken);
            long expirationTime = System.currentTimeMillis() + expirationTimeMillis;
            
            blacklistedRefreshTokens.put(key, expirationTime);
            log.info(" [TokenBlacklistService] Refresh Token 블랙리스트 추가 완료: {}",
                    key.substring(0, Math.min(20, key.length())) + "...");
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] Refresh Token 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + generateTokenHash(token);
            Long expirationTime = blacklistedTokens.get(key);
            
            if (expirationTime == null) {
                return false;
            }

            if (System.currentTimeMillis() > expirationTime) {
                blacklistedTokens.remove(key);
                log.debug(" [TokenBlacklistService] 만료된 Access Token 제거: {}",
                        key.substring(0, Math.min(20, key.length())) + "...");
                return false;
            }
            
            log.debug(" [TokenBlacklistService] 블랙리스트된 Access Token 발견: {}",
                    key.substring(0, Math.min(20, key.length())) + "...");
            return true;
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] Access Token 블랙리스트 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isRefreshTokenBlacklisted(String refreshToken) {
        try {
            String key = REFRESH_BLACKLIST_PREFIX + generateTokenHash(refreshToken);
            Long expirationTime = blacklistedRefreshTokens.get(key);
            
            if (expirationTime == null) {
                return false;
            }

            if (System.currentTimeMillis() > expirationTime) {
                blacklistedRefreshTokens.remove(key);
                log.debug(" [TokenBlacklistService] 만료된 Refresh Token 제거: {}",
                        key.substring(0, Math.min(20, key.length())) + "...");
                return false;
            }
            
            log.debug(" [TokenBlacklistService] 블랙리스트된 Refresh Token 발견: {}",
                    key.substring(0, Math.min(20, key.length())) + "...");
            return true;
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] Refresh Token 블랙리스트 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }
    public void recordUserLogout(Long userId, long logoutTime) {
        try {
            userLogoutTimes.put(userId, logoutTime);
            log.info(" [TokenBlacklistService] 사용자 로그아웃 기록: userId={}, logoutTime={}",
                    userId, logoutTime);
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 로그아웃 기록 실패: {}", e.getMessage(), e);
        }
    }

    public Long getUserLogoutTime(Long userId) {
        try {
            Long logoutTime = userLogoutTimes.get(userId);
            if (logoutTime == null) {
                return null;
            }
            
            // 24시간이 지난 로그아웃 정보는 제거
            if (System.currentTimeMillis() - logoutTime > TimeUnit.HOURS.toMillis(24)) {
                userLogoutTimes.remove(userId);
                log.debug(" [TokenBlacklistService] 오래된 로그아웃 정보 제거: userId={}", userId);
                return null;
            }
            
            return logoutTime;
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 로그아웃 시간 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public BlacklistStats getBlacklistStats() {
        try {
            int totalBlacklisted = blacklistedTokens.size();
            int totalRefreshBlacklisted = blacklistedRefreshTokens.size();
            int totalLogoutRecords = userLogoutTimes.size();

            long currentTime = System.currentTimeMillis();
            int expiredTokens = (int) blacklistedTokens.values().stream()
                    .filter(expirationTime -> currentTime > expirationTime)
                    .count();
            
            int expiredRefreshTokens = (int) blacklistedRefreshTokens.values().stream()
                    .filter(expirationTime -> currentTime > expirationTime)
                    .count();
            
            return new BlacklistStats(totalBlacklisted, totalRefreshBlacklisted, expiredTokens, expiredRefreshTokens, totalLogoutRecords);
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 블랙리스트 통계 조회 실패: {}", e.getMessage(), e);
            return new BlacklistStats(0, 0, 0, 0, 0);
        }
    }

    public void cleanupExpiredTokens() {
        try {
            long currentTime = System.currentTimeMillis();
            AtomicInteger removedCount = new AtomicInteger(0);

            blacklistedTokens.entrySet().removeIf(entry -> {
                if (currentTime > entry.getValue()) {
                    removedCount.incrementAndGet();
                    return true;
                }
                return false;
            });

            AtomicInteger removedRefreshCount = new AtomicInteger(0);
            blacklistedRefreshTokens.entrySet().removeIf(entry -> {
                if (currentTime > entry.getValue()) {
                    removedRefreshCount.incrementAndGet();
                    return true;
                }
                return false;
            });
            
            // 24시간이 지난 로그아웃 정보 제거
            AtomicInteger removedLogoutCount = new AtomicInteger(0);
            userLogoutTimes.entrySet().removeIf(entry -> {
                if (currentTime - entry.getValue() > TimeUnit.HOURS.toMillis(24)) {
                    removedLogoutCount.incrementAndGet();
                    return true;
                }
                return false;
            });
            
            int finalRemovedCount = removedCount.get();
            int finalRemovedRefreshCount = removedRefreshCount.get();
            int finalRemovedLogoutCount = removedLogoutCount.get();
            
            if (finalRemovedCount > 0 || finalRemovedRefreshCount > 0 || finalRemovedLogoutCount > 0) {
                log.info(" [TokenBlacklistService] 정리 완료: 만료된 Access Token {}개, Refresh Token {}개, 오래된 로그아웃 기록 {}개 제거",
                        finalRemovedCount, finalRemovedRefreshCount, finalRemovedLogoutCount);
            }
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 만료된 토큰 정리 실패: {}", e.getMessage(), e);
        }
    }

    private String generateTokenHash(String token) {
        try {
            return java.security.MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes())
                    .toString();
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 토큰 해시 생성 실패: {}", e.getMessage(), e);
            return token;
        }
    }

    public static class BlacklistStats {
        private final int totalBlacklisted;
        private final int totalRefreshBlacklisted;
        private final int expiredTokens;
        private final int expiredRefreshTokens;
        private final int totalLogoutRecords;

        public BlacklistStats(int totalBlacklisted, int totalRefreshBlacklisted, int expiredTokens, int expiredRefreshTokens, int totalLogoutRecords) {
            this.totalBlacklisted = totalBlacklisted;
            this.totalRefreshBlacklisted = totalRefreshBlacklisted;
            this.expiredTokens = expiredTokens;
            this.expiredRefreshTokens = expiredRefreshTokens;
            this.totalLogoutRecords = totalLogoutRecords;
        }

        public int getTotalBlacklisted() { return totalBlacklisted; }
        public int getTotalRefreshBlacklisted() { return totalRefreshBlacklisted; }
        public int getExpiredTokens() { return expiredTokens; }
        public int getExpiredRefreshTokens() { return expiredRefreshTokens; }
        public int getTotalLogoutRecords() { return totalLogoutRecords; }
        public int getActiveBlacklisted() { return totalBlacklisted - expiredTokens; }
        public int getActiveRefreshBlacklisted() { return totalRefreshBlacklisted - expiredRefreshTokens; }

        @Override
        public String toString() {
            return String.format("BlacklistStats{totalBlacklisted=%d, totalRefreshBlacklisted=%d, expiredTokens=%d, expiredRefreshTokens=%d, totalLogoutRecords=%d, activeBlacklisted=%d, activeRefreshBlacklisted=%d}", 
                    totalBlacklisted, totalRefreshBlacklisted, expiredTokens, expiredRefreshTokens, totalLogoutRecords, getActiveBlacklisted(), getActiveRefreshBlacklisted());
        }
    }
}