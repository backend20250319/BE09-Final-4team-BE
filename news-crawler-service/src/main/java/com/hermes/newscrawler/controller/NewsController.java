package com.hermes.newscrawler.controller;

import com.hermes.newscrawler.entity.NewsArticle;
import com.hermes.newscrawler.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {

    private final NewsArticleService newsArticleService;

    @GetMapping
    public ResponseEntity<List<NewsArticle>> getAllNews() {
        List<NewsArticle> news = newsArticleService.getAllNewsArticles();
        return ResponseEntity.ok(news);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<NewsArticle>> getRecentNews() {
        List<NewsArticle> news = newsArticleService.getRecentNewsArticles();
        return ResponseEntity.ok(news);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsArticle> getNewsById(@PathVariable Long id) {
        Optional<NewsArticle> news = newsArticleService.getNewsArticleById(id);
        return news.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<NewsArticle>> getNewsByCategory(@PathVariable Integer categoryId) {
        List<NewsArticle> news = newsArticleService.getNewsArticlesByCategory(categoryId);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/press/{press}")
    public ResponseEntity<List<NewsArticle>> getNewsByPress(@PathVariable String press) {
        List<NewsArticle> news = newsArticleService.getNewsArticlesByPress(press);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/search")
    public ResponseEntity<List<NewsArticle>> searchNewsByTitle(@RequestParam String title) {
        List<NewsArticle> news = newsArticleService.searchNewsArticlesByTitle(title);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getNewsCount() {
        long count = newsArticleService.getNewsArticleCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/category/{categoryId}/count")
    public ResponseEntity<Long> getNewsCountByCategory(@PathVariable Integer categoryId) {
        List<NewsArticle> news = newsArticleService.getNewsArticlesByCategory(categoryId);
        return ResponseEntity.ok((long) news.size());
    }
}