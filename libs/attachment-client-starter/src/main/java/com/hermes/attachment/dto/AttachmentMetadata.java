package com.hermes.attachment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentMetadata {
    
    private String fileId;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String filePath;
}