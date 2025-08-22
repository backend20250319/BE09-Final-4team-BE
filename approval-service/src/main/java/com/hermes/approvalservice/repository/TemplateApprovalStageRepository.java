package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.TemplateApprovalStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateApprovalStageRepository extends JpaRepository<TemplateApprovalStage, Long> {

    List<TemplateApprovalStage> findByTemplateIdOrderByStageOrderAsc(Long templateId);

    void deleteByTemplateId(Long templateId);
}