package com.hermes.jwt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// JWT 토큰 블랙리스트 서비스 - 로그아웃된 Access Token과 Refresh Token을 관리하고 검증하는 서비스

@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blacklistedRefreshTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userLogoutTimes = new ConcurrentHashMap<>();
    
    // 사용자별 토큰 추적을 위한 새로운 맵들
    private final ConcurrentHashMap<Long, Set<String>> userAccessTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<String>> userRefreshTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> userTokenExpirationTimes = new ConcurrentHashMap<>();

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_BLACKLIST_PREFIX = "jwt:refresh:blacklist:";
    private static final String LOGOUT_PREFIX = "jwt:logout:";
    private static final String USER_TOKENS_PREFIX = "jwt:user:tokens:";

    /**
     * 토큰을 블랙리스트에 추가하고 사용자별 토큰 추적
     */
    public void blacklistToken(String token, long expirationTimeMillis, Long userId) {
        try {
            String key = BLACKLIST_PREFIX + generateTokenHash(token);
            long expirationTime = System.currentTimeMillis() + expirationTimeMillis;

            blacklistedTokens.put(key, expirationTime);
            
            // 사용자별 토큰 추적
            if (userId != null) {
                userAccessTokens.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(key);
                userTokenExpirationTimes.put(userId, Math.max(
                    userTokenExpirationTimes.getOrDefault(userId, 0L), 
                    expirationTime
                ));
            }
            
            log.info(" [TokenBlacklistService] Access Token 블랙리스트 추가 완료: userId={}, token={}",
                    userId, key.substring(0, Math.min(20, key.length())) + "...");
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] Access Token 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * Refresh 토큰을 블랙리스트에 추가하고 사용자별 토큰 추적
     */
    public void blacklistRefreshToken(String refreshToken, long expirationTimeMillis, Long userId) {
        try {
            String key = REFRESH_BLACKLIST_PREFIX + generateTokenHash(refreshToken);
            long expirationTime = System.currentTimeMillis() + expirationTimeMillis;

            blacklistedRefreshTokens.put(key, expirationTime);
            
            // 사용자별 토큰 추적
            if (userId != null) {
                userRefreshTokens.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(key);
                userTokenExpirationTimes.put(userId, Math.max(
                    userTokenExpirationTimes.getOrDefault(userId, 0L), 
                    expirationTime
                ));
            }
            
            log.info(" [TokenBlacklistService] Refresh Token 블랙리스트 추가 완료: userId={}, token={}",
                    userId, key.substring(0, Math.min(20, key.length())) + "...");
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] Refresh Token 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 사용자 로그아웃 시 모든 활성 토큰을 즉시 만료
     */
    public void forceExpireAllUserTokens(Long userId) {
        try {
            long currentTime = System.currentTimeMillis();
            
            // 사용자의 모든 Access Token을 즉시 만료
            Set<String> userAccessTokenKeys = userAccessTokens.get(userId);
            if (userAccessTokenKeys != null) {
                for (String tokenKey : userAccessTokenKeys) {
                    blacklistedTokens.put(tokenKey, currentTime); // 즉시 만료
                }
                log.info(" [TokenBlacklistService] 사용자 {}의 모든 Access Token 즉시 만료 처리 완료 ({}개)", 
                        userId, userAccessTokenKeys.size());
            }
            
            // 사용자의 모든 Refresh Token을 즉시 만료
            Set<String> userRefreshTokenKeys = userRefreshTokens.get(userId);
            if (userRefreshTokenKeys != null) {
                for (String tokenKey : userRefreshTokenKeys) {
                    blacklistedRefreshTokens.put(tokenKey, currentTime); // 즉시 만료
                }
                log.info(" [TokenBlacklistService] 사용자 {}의 모든 Refresh Token 즉시 만료 처리 완료 ({}개)", 
                        userId, userRefreshTokenKeys.size());
            }
            
            // 사용자 토큰 만료 시간을 현재 시간으로 설정
            userTokenExpirationTimes.put(userId, currentTime);
            
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 토큰 강제 만료 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 사용자의 모든 토큰을 완전히 삭제
     */
    public void deleteAllUserTokens(Long userId) {
        try {
            // 사용자의 모든 Access Token 삭제
            Set<String> userAccessTokenKeys = userAccessTokens.remove(userId);
            if (userAccessTokenKeys != null) {
                for (String tokenKey : userAccessTokenKeys) {
                    blacklistedTokens.remove(tokenKey);
                }
                log.info(" [TokenBlacklistService] 사용자 {}의 모든 Access Token 삭제 완료 ({}개)", 
                        userId, userAccessTokenKeys.size());
            }
            
            // 사용자의 모든 Refresh Token 삭제
            Set<String> userRefreshTokenKeys = userRefreshTokens.remove(userId);
            if (userRefreshTokenKeys != null) {
                for (String tokenKey : userRefreshTokenKeys) {
                    blacklistedRefreshTokens.remove(tokenKey);
                }
                log.info(" [TokenBlacklistService] 사용자 {}의 모든 Refresh Token 삭제 완료 ({}개)", 
                        userId, userRefreshTokenKeys.size());
            }
            
            // 사용자 토큰 만료 시간 정보 삭제
            userTokenExpirationTimes.remove(userId);
            
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 토큰 삭제 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 사용자별 토큰 정보 조회
     */
    public UserTokenInfo getUserTokenInfo(Long userId) {
        try {
            Set<String> accessTokens = userAccessTokens.getOrDefault(userId, Collections.emptySet());
            Set<String> refreshTokens = userRefreshTokens.getOrDefault(userId, Collections.emptySet());
            Long expirationTime = userTokenExpirationTimes.get(userId);
            
            int activeAccessTokens = (int) accessTokens.stream()
                    .filter(tokenKey -> {
                        Long tokenExpiration = blacklistedTokens.get(tokenKey);
                        return tokenExpiration != null && System.currentTimeMillis() < tokenExpiration;
                    })
                    .count();
                    
            int activeRefreshTokens = (int) refreshTokens.stream()
                    .filter(tokenKey -> {
                        Long tokenExpiration = blacklistedRefreshTokens.get(tokenKey);
                        return tokenExpiration != null && System.currentTimeMillis() < tokenExpiration;
                    })
                    .count();
            
            return new UserTokenInfo(userId, accessTokens.size(), refreshTokens.size(), 
                    activeAccessTokens, activeRefreshTokens, expirationTime);
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 토큰 정보 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return new UserTokenInfo(userId, 0, 0, 0, 0, null);
        }
    }

    /**
     * 만료된 토큰들을 완전히 삭제 (메모리 정리)
     */
    public void deleteExpiredTokens() {
        try {
            long currentTime = System.currentTimeMillis();
            AtomicInteger deletedAccessCount = new AtomicInteger(0);
            AtomicInteger deletedRefreshCount = new AtomicInteger(0);

            // 만료된 Access Token 삭제
            blacklistedTokens.entrySet().removeIf(entry -> {
                if (currentTime > entry.getValue()) {
                    deletedAccessCount.incrementAndGet();
                    return true;
                }
                return false;
            });

            // 만료된 Refresh Token 삭제
            blacklistedRefreshTokens.entrySet().removeIf(entry -> {
                if (currentTime > entry.getValue()) {
                    deletedRefreshCount.incrementAndGet();
                    return true;
                }
                return false;
            });

            // 사용자별 토큰 맵에서도 만료된 토큰 제거
            cleanupUserTokenMaps();

            int finalDeletedAccess = deletedAccessCount.get();
            int finalDeletedRefresh = deletedRefreshCount.get();
            
            if (finalDeletedAccess > 0 || finalDeletedRefresh > 0) {
                log.info(" [TokenBlacklistService] 만료된 토큰 삭제 완료: Access Token {}개, Refresh Token {}개", 
                        finalDeletedAccess, finalDeletedRefresh);
            }
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 만료된 토큰 삭제 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 사용자별 토큰 맵에서 만료된 토큰 정리
     */
    private void cleanupUserTokenMaps() {
        long currentTime = System.currentTimeMillis();
        
        // Access Token 맵 정리
        userAccessTokens.forEach((userId, tokenKeys) -> {
            tokenKeys.removeIf(tokenKey -> {
                Long expirationTime = blacklistedTokens.get(tokenKey);
                return expirationTime == null || currentTime > expirationTime;
            });
        });
        
        // Refresh Token 맵 정리
        userRefreshTokens.forEach((userId, tokenKeys) -> {
            tokenKeys.removeIf(tokenKey -> {
                Long expirationTime = blacklistedRefreshTokens.get(tokenKey);
                return expirationTime == null || currentTime > expirationTime;
            });
        });
        
        // 빈 사용자 토큰 맵 제거
        userAccessTokens.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        userRefreshTokens.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * 사용자 로그아웃 처리 - 모든 토큰을 완전히 삭제
     * 
     * @param userId 로그아웃할 사용자 ID
     * @param accessToken 현재 액세스 토큰 (선택사항)
     * @param refreshToken 현재 리프레시 토큰 (선택사항)
     */
    public void logoutUser(Long userId, String accessToken, String refreshToken) {
        try {
            // 개별 토큰을 블랙리스트에 추가 (즉시 만료)
            if (accessToken != null) {
                blacklistToken(accessToken, 0, userId);
            }
            if (refreshToken != null) {
                blacklistRefreshToken(refreshToken, 0, userId);
            }
            
            // 사용자의 모든 토큰을 완전히 삭제
            deleteAllUserTokens(userId);
            
            // 로그아웃 시간 기록
            recordUserLogout(userId, System.currentTimeMillis());
            
            log.info(" [TokenBlacklistService] 사용자 {} 로그아웃 처리 완료 - 모든 토큰 삭제됨", userId);
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 로그아웃 처리 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
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
     * 배치로 여러 사용자의 토큰을 만료
     */
    public void batchExpireUserTokens(List<Long> userIds) {
        try {
            for (Long userId : userIds) {
                forceExpireAllUserTokens(userId);
            }
            log.info(" [TokenBlacklistService] 배치 토큰 만료 완료: {}명의 사용자", userIds.size());
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 배치 토큰 만료 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 배치로 여러 사용자의 토큰을 삭제
     */
    public void batchDeleteUserTokens(List<Long> userIds) {
        try {
            for (Long userId : userIds) {
                deleteAllUserTokens(userId);
            }
            log.info(" [TokenBlacklistService] 배치 토큰 삭제 완료: {}명의 사용자", userIds.size());
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 배치 토큰 삭제 실패: {}", e.getMessage(), e);
        }
    }

    // 기존 메서드들과의 호환성을 위한 오버로드
    public void blacklistToken(String token, long expirationTimeMillis) {
        blacklistToken(token, expirationTimeMillis, null);
    }

    public void blacklistRefreshToken(String refreshToken, long expirationTimeMillis) {
        blacklistRefreshToken(refreshToken, expirationTimeMillis, null);
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
            String key = LOGOUT_PREFIX + userId;
            userLogoutTimes.put(key, logoutTime);
            log.info(" [TokenBlacklistService] 사용자 로그아웃 시간 기록 완료: userId={}, logoutTime={}", userId, logoutTime);
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 로그아웃 시간 기록 실패: {}", e.getMessage(), e);
        }
    }

    public Long getUserLogoutTime(Long userId) {
        try {
            String key = LOGOUT_PREFIX + userId;
            Long logoutTime = userLogoutTimes.get(key);
            if (logoutTime == null) {
                return null;
            }

            // 24시간이 지난 로그아웃 정보는 제거
            if (System.currentTimeMillis() - logoutTime > TimeUnit.HOURS.toMillis(24)) {
                userLogoutTimes.remove(key);
                log.debug(" [TokenBlacklistService] 오래된 로그아웃 정보 제거: userId={}", userId);
                return null;
            }

            return logoutTime;
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 로그아웃 시간 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean isUserLoggedOut(Long userId) {
        try {
            String key = LOGOUT_PREFIX + userId;
            Long logoutTime = userLogoutTimes.get(key);
            return logoutTime != null;
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 로그아웃 상태 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    public void removeUserLogoutRecord(Long userId) {
        try {
            String key = LOGOUT_PREFIX + userId;
            userLogoutTimes.remove(key);
            log.info(" [TokenBlacklistService] 사용자 로그아웃 기록 제거 완료: userId={}", userId);
        } catch (Exception e) {
            log.error(" [TokenBlacklistService] 사용자 로그아웃 기록 제거 실패: {}", e.getMessage(), e);
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

    /**
     * 사용자별 토큰 정보를 담는 클래스
     */
    public static class UserTokenInfo {
        private final Long userId;
        private final int totalAccessTokens;
        private final int totalRefreshTokens;
        private final int activeAccessTokens;
        private final int activeRefreshTokens;
        private final Long expirationTime;

        public UserTokenInfo(Long userId, int totalAccessTokens, int totalRefreshTokens, 
                           int activeAccessTokens, int activeRefreshTokens, Long expirationTime) {
            this.userId = userId;
            this.totalAccessTokens = totalAccessTokens;
            this.totalRefreshTokens = totalRefreshTokens;
            this.activeAccessTokens = activeAccessTokens;
            this.activeRefreshTokens = activeRefreshTokens;
            this.expirationTime = expirationTime;
        }

        public Long getUserId() { return userId; }
        public int getTotalAccessTokens() { return totalAccessTokens; }
        public int getTotalRefreshTokens() { return totalRefreshTokens; }
        public int getActiveAccessTokens() { return activeAccessTokens; }
        public int getActiveRefreshTokens() { return activeRefreshTokens; }
        public Long getExpirationTime() { return expirationTime; }

        @Override
        public String toString() {
            return String.format("UserTokenInfo{userId=%d, totalAccessTokens=%d, totalRefreshTokens=%d, activeAccessTokens=%d, activeRefreshTokens=%d, expirationTime=%d}",
                    userId, totalAccessTokens, totalRefreshTokens, activeAccessTokens, activeRefreshTokens, expirationTime);
        }
    }
}