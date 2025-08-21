package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.DocumentActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentActivityRepository extends JpaRepository<DocumentActivity, Long> {

    List<DocumentActivity> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
}