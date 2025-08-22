package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.DocumentApprovalTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentApprovalTargetRepository extends JpaRepository<DocumentApprovalTarget, Long> {

    List<DocumentApprovalTarget> findByDocumentId(Long documentId);

    List<DocumentApprovalTarget> findByApprovalStageId(Long stageId);

    List<DocumentApprovalTarget> findByDocumentIdAndIsReferenceTrue(Long documentId);

    @Query("SELECT t FROM DocumentApprovalTarget t WHERE t.userId = :userId AND t.isApproved = false " +
           "AND t.approvalStage.stageOrder = t.document.currentStage AND t.document.status = 'IN_PROGRESS'")
    List<DocumentApprovalTarget> findPendingApprovalsForUser(@Param("userId") Long userId);

    void deleteByDocumentId(Long documentId);

    void deleteByApprovalStageId(Long stageId);
}