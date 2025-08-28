package com.hermes.approvalservice.dto.response;

import lombok.Data;

@Data
public class AttachmentInfoResponse {
    
    private String fileId;
    private String displayFileName;
    private Long fileSize;
    private String contentType;
    private String downloadUrl;
}