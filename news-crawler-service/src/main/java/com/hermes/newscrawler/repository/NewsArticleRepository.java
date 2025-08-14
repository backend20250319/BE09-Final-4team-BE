package com.hermes.newscrawler.repository;

import com.hermes.newscrawler.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    List<NewsArticle> findByCategoryIdOrderByCreatedAtDesc(Integer categoryId);

    List<NewsArticle> findByPressOrderByCreatedAtDesc(String press);

    List<NewsArticle> findByTitleContainingOrderByCreatedAtDesc(String title);

    List<NewsArticle> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT n FROM NewsArticle n WHERE n.categoryId = :categoryId ORDER BY n.createdAt DESC")
    List<NewsArticle> findRecentByCategory(@Param("categoryId") Integer categoryId);

    boolean existsByLink(String link);
}
