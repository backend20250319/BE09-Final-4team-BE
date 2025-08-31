package com.hermes.attachment.client;

import com.hermes.api.common.ApiResult;
import com.hermes.attachment.dto.AttachmentInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AttachmentServiceClientFallback implements AttachmentServiceClient {
    
    @Override
    public ApiResult<AttachmentInfoResponse> getFileMetadata(String fileId) {
        log.warn("attachment-service is not available, returning mock data for fileId: {}", fileId);
        
        AttachmentInfoResponse response = new AttachmentInfoResponse();
        response.setFileId(fileId);
        response.setFileName("mock-file.pdf");
        response.setFileSize(1024L);
        response.setContentType("application/pdf");
        
        return ApiResult.success(response);
    }
}