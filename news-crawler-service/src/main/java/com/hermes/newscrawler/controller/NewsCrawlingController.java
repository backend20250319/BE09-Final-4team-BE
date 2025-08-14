package com.hermes.newscrawler.controller;

import com.hermes.newscrawler.dto.NewsDetail;
import com.hermes.newscrawler.entity.NewsArticle;
import com.hermes.newscrawler.service.NewsArticleService;
import com.hermes.newscrawler.util.DatabaseCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/news-crawling")
@RequiredArgsConstructor
public class NewsCrawlingController {
    
    private final DatabaseCrawler databaseCrawler;
    private final NewsArticleService newsArticleService;

    @PostMapping("/login-crawling")
    public ResponseEntity<Map<String, Object>> startCrawlingForLogin(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String email = request.get("email");

            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "사용자 ID가 필요합니다.",
                    "error", "userId is required"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String sessionId = UUID.randomUUID().toString();
            
            log.info(" [News Crawling] 로그인 시 뉴스 크롤링 시작 - userId: {}, sessionId: {}", userId, sessionId);

            List<NewsDetail> newsList = databaseCrawler.performCrawlingForLogin(sessionId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "로그인 시 뉴스 크롤링 완료",
                "sessionId", sessionId,
                "newsCount", newsList.size(),
                "newsList", newsList,
                "timestamp", System.currentTimeMillis()
            );
            
            log.info(" [News Crawling] 로그인 시 뉴스 크롤링 완료 - userId: {}, newsCount: {}", userId, newsList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error(" [News Crawling] 로그인 시 뉴스 크롤링 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "뉴스 크롤링 실패: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getNewsForSession(@PathVariable String sessionId) {
        try {
            List<NewsDetail> newsList = databaseCrawler.getNewsForSession(sessionId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "sessionId", sessionId,
                "newsCount", newsList.size(),
                "newsList", newsList
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error(" [News Crawling] 세션 뉴스 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "세션 뉴스 조회 실패: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/logout-cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOnLogout(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            
            log.info(" [News Crawling] 로그아웃 시 세션 데이터 정리 - sessionId: {}", sessionId);

            databaseCrawler.clearSessionData(sessionId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "세션 데이터 정리 완료",
                "sessionId", sessionId
            );
            
            log.info(" [News Crawling] 로그아웃 시 세션 데이터 정리 완료 - sessionId: {}", sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error(" [News Crawling] 로그아웃 시 세션 데이터 정리 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "세션 데이터 정리 실패: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/db-articles")
    public ResponseEntity<Map<String, Object>> getDbArticles() {
        try {
            List<NewsArticle> articles = newsArticleService.getAllNewsArticles();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "totalCount", articles.size(),
                "articles", articles
            );
            
            log.info(" [News Crawling] DB 뉴스 기사 조회 완료 - 총 {}개", articles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error(" [News Crawling] DB 뉴스 기사 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "DB 뉴스 기사 조회 실패: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
