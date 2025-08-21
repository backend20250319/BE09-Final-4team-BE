package com.hermes.approvalservice.repository;

import com.hermes.approvalservice.entity.DocumentFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentFieldValueRepository extends JpaRepository<DocumentFieldValue, Long> {

    List<DocumentFieldValue> findByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);
}