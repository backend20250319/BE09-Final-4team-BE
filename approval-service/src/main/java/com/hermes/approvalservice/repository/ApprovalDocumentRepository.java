package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long> {

    Page<ApprovalDocument> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<ApprovalDocument> findByStatusOrderByCreatedAtDesc(DocumentStatus status, Pageable pageable);

    List<ApprovalDocument> findByTemplateId(Long templateId);

    @Query("SELECT d FROM ApprovalDocument d WHERE " +
           "(d.authorId = :userId OR EXISTS (SELECT 1 FROM DocumentApprovalTarget t WHERE t.document = d AND t.userId = :userId)) " +
           "AND (:statuses IS NULL OR d.status IN :statuses) " +
           "AND (:search IS NULL OR LOWER(d.template.title) LIKE LOWER(CONCAT('%', CAST(:search AS STRING), '%')) " +
           "    OR (:authorIds IS NOT NULL AND d.authorId IN :authorIds)) " +
           "AND (CAST(:startDateTime AS java.time.LocalDateTime) IS NULL OR d.createdAt >= :startDateTime) " +
           "AND (CAST(:endDateTime AS java.time.LocalDateTime) IS NULL OR d.createdAt <= :endDateTime) " +
           "ORDER BY d.createdAt DESC")
    Page<ApprovalDocument> findDocumentsForUserWithFilters(
            @Param("userId") Long userId,
            @Param("statuses") List<DocumentStatus> statuses,
            @Param("search") String search,
            @Param("authorIds") List<Long> authorIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);
}