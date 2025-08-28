package com.hermes.newscrawler.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hermes.newscrawler.util.DatabaseCrawler;
import com.hermes.newscrawler.service.NewsArticleService;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewsCrawlingScheduler {

    private final DatabaseCrawler databaseCrawler;
    private final NewsArticleService newsArticleService;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void dailyNewsRefresh() {
        try {
            newsArticleService.deleteAllNews();
            String sessionId = "daily-" + System.currentTimeMillis();
            databaseCrawler.performCrawlingForLogin(sessionId);
        } catch (Exception e) {
            log.error("매일 뉴스 새로고침 실패: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredSessions() {
        try {
            databaseCrawler.cleanupExpiredSessions();
            databaseCrawler.logMemoryUsage();
        } catch (Exception e) {
            log.error("세션 데이터 정리 실패: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void fullCleanup() {
        try {
            databaseCrawler.clearAllSessionData();
            databaseCrawler.logMemoryUsage();
        } catch (Exception e) {
            log.error("전체 세션 데이터 정리 실패: {}", e.getMessage(), e);
        }
    }
}