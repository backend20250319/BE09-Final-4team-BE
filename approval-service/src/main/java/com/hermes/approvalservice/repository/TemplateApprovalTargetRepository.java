package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.TemplateApprovalTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateApprovalTargetRepository extends JpaRepository<TemplateApprovalTarget, Long> {

    List<TemplateApprovalTarget> findByTemplateId(Long templateId);

    List<TemplateApprovalTarget> findByApprovalStageId(Long stageId);

    List<TemplateApprovalTarget> findByTemplateIdAndIsReferenceTrue(Long templateId);

    void deleteByTemplateId(Long templateId);

    void deleteByApprovalStageId(Long stageId);
}