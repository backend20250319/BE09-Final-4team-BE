package com.hermes.approvalservice.dto.response;

import com.hermes.attachment.dto.AttachmentInfoResponse;
import com.hermes.approvalservice.enums.DocumentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentResponse {
    
    private Long id;
    private String title;
    private String content;
    private DocumentStatus status;
    private Long authorId;
    private Integer currentStage;
    private TemplateResponse template;
    private List<DocumentFieldValueResponse> fieldValues;
    private List<ApprovalStageResponse> approvalStages;
    private List<ApprovalTargetResponse> referenceTargets;
    private List<DocumentActivityResponse> activities;
    private List<DocumentCommentResponse> comments;
    private List<AttachmentInfoResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
}