package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentSummaryResponse {
    
    private Long id;
    private String content;
    private DocumentStatus status;
    private UserProfile author;
    private String templateTitle;
    private Integer currentStage;
    private Integer totalStages;
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
}