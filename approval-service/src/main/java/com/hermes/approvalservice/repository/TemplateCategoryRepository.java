package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.TemplateCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateCategoryRepository extends JpaRepository<TemplateCategory, Long> {

    List<TemplateCategory> findAllByOrderBySortOrderAsc();

    Optional<TemplateCategory> findByName(String name);

    @Query("SELECT c FROM TemplateCategory c WHERE EXISTS (SELECT 1 FROM DocumentTemplate t WHERE t.category = c AND t.isHidden = false) ORDER BY c.sortOrder ASC")
    List<TemplateCategory> findCategoriesWithVisibleTemplates();
}