package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    List<DocumentTemplate> findByIsHiddenFalse();

    List<DocumentTemplate> findByCategoryIdAndIsHiddenFalse(Long categoryId);

    List<DocumentTemplate> findByCategoryId(Long categoryId);

    @Query("SELECT t FROM DocumentTemplate t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.fields LEFT JOIN FETCH t.approvalStages LEFT JOIN FETCH t.referenceTargets WHERE t.id = :id")
    DocumentTemplate findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT t FROM DocumentTemplate t LEFT JOIN FETCH t.category WHERE t.isHidden = false ORDER BY t.category.sortOrder ASC, t.createdAt ASC")
    List<DocumentTemplate> findVisibleTemplatesWithCategory();
}