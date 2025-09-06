package com.hermes.attachment.client;

import com.hermes.api.common.ApiResult;
import com.hermes.attachment.dto.AttachmentInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "attachment-service")
public interface AttachmentServiceClient {
    
    @GetMapping("/api/attachments/{fileId}/info")
    AttachmentInfoResponse getFileMetadata(@PathVariable("fileId") String fileId);
}