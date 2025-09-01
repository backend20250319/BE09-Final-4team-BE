package com.hermes.attachment.dto;

import lombok.Data;

@Data
public class AttachmentInfoResponse {
    
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String contentType;
}