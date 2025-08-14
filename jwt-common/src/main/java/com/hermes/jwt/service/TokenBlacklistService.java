package com.hermes.jwt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> userLogoutTimes = new ConcurrentHashMap<>();
    
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String LOGOUT_PREFIX = "jwt:logout:";

    public void blacklistToken(String token, long expirationTimeMillis) {
        try {
            String key = BLACKLIST_PREFIX + generateTokenHash(token);
            long expirationTime = System.currentTimeMillis() + expirationTimeMillis;
            
            blacklistedTokens.put(key, expirationTime);
            log.debug("Token blacklisted: {}", key.substring(0, Math.min(20, key.length())));
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
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
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to check token blacklist", e);
            return false;
        }
    }

    public void recordUserLogout(Long userId, long logoutTime) {
        try {
            userLogoutTimes.put(userId, logoutTime);
            log.debug("User logout recorded: userId={}, logoutTime={}", userId, logoutTime);
        } catch (Exception e) {
            log.error("Failed to record user logout", e);
        }
    }

    public Long getUserLogoutTime(Long userId) {
        try {
            Long logoutTime = userLogoutTimes.get(userId);
            if (logoutTime == null) {
                return null;
            }

            if (System.currentTimeMillis() - logoutTime > TimeUnit.HOURS.toMillis(24)) {
                userLogoutTimes.remove(userId);
                return null;
            }
            
            return logoutTime;
        } catch (Exception e) {
            log.error("Failed to get user logout time", e);
            return null;
        }
    }

    private String generateTokenHash(String token) {
        try {
            return java.security.MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes())
                    .toString();
        } catch (Exception e) {
            log.error("Failed to generate token hash", e);
            return token; // fallback
        }
    }
}
