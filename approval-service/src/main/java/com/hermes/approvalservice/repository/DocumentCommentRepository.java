package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {

    List<DocumentComment> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
}