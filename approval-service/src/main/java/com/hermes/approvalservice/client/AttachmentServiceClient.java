package com.hermes.approvalservice.client;

import com.hermes.approvalservice.dto.AttachmentMetadata;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "attachment-service", fallback = AttachmentServiceClientFallback.class)
public interface AttachmentServiceClient {
    
    @GetMapping("/internal/attachments/{fileId}/metadata")
    AttachmentMetadata getFileMetadata(@PathVariable String fileId);
}