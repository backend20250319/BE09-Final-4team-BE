package com.hermes.newscrawler.service;

import com.hermes.newscrawler.dto.NewsDetail;
import com.hermes.newscrawler.entity.NewsArticle;
import com.hermes.newscrawler.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;

    public NewsArticle saveNewsArticle(NewsDetail newsDetail) {
        if (newsDetail.getLink() == null || newsDetail.getLink().trim().isEmpty()) {
            return null;
        }

        String normalizedLink = normalizeLink(newsDetail.getLink());
        if (newsArticleRepository.existsByLink(normalizedLink)) {
            return null;
        }

        NewsArticle newsArticle = NewsArticle.builder()
                .categoryId(newsDetail.getCategoryId())
                .categoryName(newsDetail.getCategoryName())
                .press(newsDetail.getPress())
                .title(newsDetail.getTitle())
                .content(newsDetail.getContent())
                .reporter(newsDetail.getReporter())
                .date(newsDetail.getDate())
                .link(normalizedLink)
                .build();

        return newsArticleRepository.save(newsArticle);
    }

    public List<NewsArticle> saveNewsArticles(List<NewsDetail> newsDetails) {
        return newsDetails.stream()
                .map(this::saveNewsArticle)
                .filter(article -> article != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> getAllNewsArticles() {
        return newsArticleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> getRecentNewsArticles() {
        return newsArticleRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> getNewsArticlesByCategory(Integer categoryId) {
        return newsArticleRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> getNewsArticlesByPress(String press) {
        return newsArticleRepository.findByPressOrderByCreatedAtDesc(press);
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> searchNewsArticlesByTitle(String title) {
        return newsArticleRepository.findByTitleContainingOrderByCreatedAtDesc(title);
    }

    @Transactional(readOnly = true)
    public Optional<NewsArticle> getNewsArticleById(Long id) {
        return newsArticleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public long getNewsArticleCount() {
        return newsArticleRepository.count();
    }

    private String normalizeLink(String link) {
        if (link == null) return "";

        String normalized = link;
        int paramIndex = link.indexOf('?');
        if (paramIndex > 0) {
            normalized = link.substring(0, paramIndex);
        }

        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized.trim();
    }

    public void deleteAllNews() {
        long count = newsArticleRepository.count();

        if (count > 0) {
            newsArticleRepository.deleteAllInBatch();

            long afterCount = newsArticleRepository.count();

            if (afterCount > 0) {
                throw new RuntimeException("데이터 삭제 실패");
            }
        }
    }
}