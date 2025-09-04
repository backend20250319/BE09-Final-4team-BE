package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.client.dto.UserProfile;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentCommentResponse {
    
    private Long id;
    private String content;
    private UserProfile author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}