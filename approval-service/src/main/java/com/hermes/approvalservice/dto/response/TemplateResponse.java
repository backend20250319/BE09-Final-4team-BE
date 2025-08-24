package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TemplateResponse {
    
    private Long id;
    private String title;
    private String icon;
    private String description;
    private String bodyTemplate;
    private Boolean useBody;
    private Boolean useAttachment;
    private Boolean allowApprovalChange;
    private Boolean allowReferenceChange;
    private Boolean isHidden;
    private String referenceFiles;
    private CategoryResponse category;
    private List<TemplateFieldResponse> fields;
    private List<ApprovalStageResponse> approvalStages;
    private List<ApprovalTargetResponse> referenceTargets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}