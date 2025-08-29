package com.hermes.attachment.dto;

import lombok.Data;

@Data
public class AttachmentMetadata {
    
    private String fileId;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String filePath;
}