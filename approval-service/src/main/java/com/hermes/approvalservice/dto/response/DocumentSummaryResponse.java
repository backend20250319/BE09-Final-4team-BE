package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.enums.DocumentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentSummaryResponse {
    
    private Long id;
    private String title;
    private DocumentStatus status;
    private Long authorId;
    private String templateTitle;
    private Integer currentStage;
    private Integer totalStages;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
}