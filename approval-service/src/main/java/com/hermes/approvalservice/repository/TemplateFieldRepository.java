package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateFieldRepository extends JpaRepository<TemplateField, Long> {

    List<TemplateField> findByTemplateIdOrderByFieldOrderAsc(Long templateId);

    void deleteByTemplateId(Long templateId);
}