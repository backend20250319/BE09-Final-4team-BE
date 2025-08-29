package com.hermes.attachment.client;

import com.hermes.attachment.dto.AttachmentMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AttachmentServiceClientFallback implements AttachmentServiceClient {
    
    @Override
    public AttachmentMetadata getFileMetadata(String fileId) {
        log.warn("attachment-service is not available, returning mock data for fileId: {}", fileId);
        
        AttachmentMetadata metadata = new AttachmentMetadata();
        metadata.setFileId(fileId);
        metadata.setOriginalFileName("mock-file.pdf");
        metadata.setFileSize(1024L);
        metadata.setContentType("application/pdf");
        metadata.setFilePath("/mock/path/" + fileId);
        
        return metadata;
    }
}