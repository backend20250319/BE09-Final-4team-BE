package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.DocumentApprovalStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentApprovalStageRepository extends JpaRepository<DocumentApprovalStage, Long> {

    List<DocumentApprovalStage> findByDocumentIdOrderByStageOrderAsc(Long documentId);

    void deleteByDocumentId(Long documentId);
}