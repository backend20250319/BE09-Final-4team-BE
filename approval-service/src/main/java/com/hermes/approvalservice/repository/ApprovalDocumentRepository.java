package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long> {

    Page<ApprovalDocument> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<ApprovalDocument> findByStatusOrderByCreatedAtDesc(DocumentStatus status, Pageable pageable);

    @Query("SELECT d FROM ApprovalDocument d WHERE d.authorId = :userId OR " +
           "EXISTS (SELECT 1 FROM DocumentApprovalTarget t WHERE t.document = d AND t.userId = :userId) " +
           "ORDER BY d.createdAt DESC")
    Page<ApprovalDocument> findDocumentsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT d FROM ApprovalDocument d JOIN d.approvalStages s JOIN s.approvalTargets t " +
           "WHERE t.userId = :userId AND t.isApproved = false AND s.stageOrder = d.currentStage " +
           "AND d.status = 'IN_PROGRESS' ORDER BY d.submittedAt ASC")
    Page<ApprovalDocument> findPendingApprovalsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT d FROM ApprovalDocument d LEFT JOIN FETCH d.template LEFT JOIN FETCH d.fieldValues " +
           "LEFT JOIN FETCH d.approvalStages LEFT JOIN FETCH d.referenceTargets WHERE d.id = :id")
    ApprovalDocument findByIdWithDetails(@Param("id") Long id);

    List<ApprovalDocument> findByTemplateId(Long templateId);
}