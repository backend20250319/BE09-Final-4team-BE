package com.hermes.attachmentservice.repository;

import com.hermes.attachmentservice.entity.AttachmentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentFileRepository extends JpaRepository<AttachmentFile, String> {
    
    List<AttachmentFile> findByUploadedBy(Long uploadedBy);
    
    boolean existsByFileId(String fileId);
}