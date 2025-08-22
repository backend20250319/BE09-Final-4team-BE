package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentCommentResponse {
    
    private Long id;
    private String content;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}