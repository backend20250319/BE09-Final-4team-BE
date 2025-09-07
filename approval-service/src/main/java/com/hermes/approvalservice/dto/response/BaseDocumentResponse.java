package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.enums.DocumentRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseDocumentResponse {
    
    protected Long id;
    protected String content;
    protected DocumentStatus status;
    protected UserProfile author;
    protected Integer currentStage;
    protected DocumentRole myRole;
    protected MyApprovalInfo myApprovalInfo;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected LocalDateTime submittedAt;
    protected LocalDateTime approvedAt;
}