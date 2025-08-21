package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentAttachmentResponse {
    
    private Long id;
    private String originalFileName;
    private String storedFileName;
    private Long fileSize;
    private String contentType;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}