package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.enums.DocumentRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentSummaryResponse {
    
    private Long id;
    private String content;
    private DocumentStatus status;
    private UserProfile author;
    private TemplateSummaryResponse template;
    private Integer currentStage;
    private Integer totalStages;
    private DocumentRole myRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
}