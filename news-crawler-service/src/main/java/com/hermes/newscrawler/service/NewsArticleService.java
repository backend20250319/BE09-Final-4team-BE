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
        if (newsArticleRepository.existsByLink(newsDetail.getLink())) {
            log.info("이미 존재하는 뉴스 링크: {}", newsDetail.getLink());
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
                .link(newsDetail.getLink())
                .build();
        
        NewsArticle saved = newsArticleRepository.save(newsArticle);
        log.info("뉴스 기사 저장 완료: {}", saved.getTitle());
        return saved;
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
}
