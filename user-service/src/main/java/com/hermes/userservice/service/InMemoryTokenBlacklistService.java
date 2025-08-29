package com.hermes.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 메모리 기반 토큰 블랙리스트 구현체
 * 실제 운영에서는 Redis 등 외부 캐시를 사용하는 것을 권장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    /**
     * 블랙리스트 토큰 정보를 저장하는 레코드
     */
    private record BlacklistEntry(Long userId, long expirationTime) {
        public boolean isExpired() {
            return Instant.now().getEpochSecond() > expirationTime;
        }
    }

    // 토큰 -> 블랙리스트 정보 매핑
    private final ConcurrentMap<String, BlacklistEntry> blacklistedTokens = new ConcurrentHashMap<>();

    @Override
    public void addToken(String token, long duration, Long userId) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("토큰이 null이거나 비어있어 블랙리스트에 추가하지 않습니다.");
            return;
        }

        long expirationTime = Instant.now().getEpochSecond() + duration;
        BlacklistEntry entry = new BlacklistEntry(userId, expirationTime);
        blacklistedTokens.put(token, entry);
        
        log.debug("토큰을 블랙리스트에 추가했습니다. 만료시간: {}", Instant.ofEpochSecond(expirationTime));
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        BlacklistEntry entry = blacklistedTokens.get(token);
        if (entry == null) {
            return false;
        }

        // 만료된 토큰은 블랙리스트에서 자동 제거하고 false 반환
        if (entry.isExpired()) {
            blacklistedTokens.remove(token);
            log.debug("만료된 토큰을 블랙리스트에서 제거했습니다.");
            return false;
        }

        return true;
    }

    @Override
    public boolean removeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        BlacklistEntry removed = blacklistedTokens.remove(token);
        if (removed != null) {
            log.debug("토큰을 블랙리스트에서 제거했습니다.");
            return true;
        }
        return false;
    }

    @Override
    @Scheduled(fixedRate = 24, timeUnit = TimeUnit.HOURS)
    public int removeExpiredTokens() {
        long currentTime = Instant.now().getEpochSecond();
        AtomicInteger removedCount = new AtomicInteger(0);

        blacklistedTokens.entrySet().removeIf(entry -> {
            if (entry.getValue().expirationTime() <= currentTime) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        int removed = removedCount.get();
        if (removed > 0) {
            log.info("만료된 토큰 {}개를 블랙리스트에서 제거했습니다.", removed);
        } else {
            log.debug("제거할 만료된 토큰이 없습니다.");
        }

        return removed;
    }

    @Override
    public int getSize() {
        return blacklistedTokens.size();
    }

    @Override
    public void clear() {
        int size = blacklistedTokens.size();
        blacklistedTokens.clear();
        log.warn("토큰 블랙리스트를 초기화했습니다. 제거된 토큰 개수: {}", size);
    }

    @Override
    public int removeUserTokens(Long userId) {
        if (userId == null) {
            return 0;
        }

        AtomicInteger removedCount = new AtomicInteger(0);
        blacklistedTokens.entrySet().removeIf(entry -> {
            Long entryUserId = entry.getValue().userId();
            if (entryUserId != null && entryUserId.equals(userId)) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        int removed = removedCount.get();
        if (removed > 0) {
            log.info("사용자 {}의 토큰 {}개를 블랙리스트에서 제거했습니다.", userId, removed);
        }

        return removed;
    }
}