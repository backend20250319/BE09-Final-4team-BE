package com.hermes.newscrawler.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hermes.newscrawler.util.DatabaseCrawler;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewsCrawlingScheduler {
    
    private final DatabaseCrawler databaseCrawler;

    @Scheduled(fixedRate = 3600000) // 1시간
    public void cleanupExpiredSessions() {
        log.info("🧹 [Scheduler] 만료된 세션 데이터 정리 시작");
        try {
            databaseCrawler.cleanupExpiredSessions();
            databaseCrawler.logMemoryUsage();
            log.info(" [Scheduler] 세션 데이터 정리 완료");
        } catch (Exception e) {
            log.error(" [Scheduler] 세션 데이터 정리 실패: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시
    public void fullCleanup() {
        log.info(" [Scheduler] 전체 세션 데이터 정리 시작");
        try {
            databaseCrawler.clearAllSessionData();
            databaseCrawler.logMemoryUsage();
            log.info(" [Scheduler] 전체 세션 데이터 정리 완료");
        } catch (Exception e) {
            log.error(" [Scheduler] 전체 세션 데이터 정리 실패: {}", e.getMessage(), e);
        }
    }
}