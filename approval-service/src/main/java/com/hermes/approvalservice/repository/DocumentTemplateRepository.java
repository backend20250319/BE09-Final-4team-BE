package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    List<DocumentTemplate> findByIsHiddenFalse();

    List<DocumentTemplate> findByCategoryIdAndIsHiddenFalse(Long categoryId);

    List<DocumentTemplate> findByCategoryId(Long categoryId);

    @Query("SELECT t FROM DocumentTemplate t LEFT JOIN FETCH t.category WHERE t.isHidden = false ORDER BY t.category.sortOrder ASC, t.createdAt ASC")
    List<DocumentTemplate> findVisibleTemplatesWithCategory();
}