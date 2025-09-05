package com.hermes.approvalservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateDocumentRequest {
    
    
    private String content;
    
    @Valid
    private List<DocumentFieldValueRequest> fieldValues;
    
    @Valid
    private List<ApprovalStageRequest> approvalStages;
    
    @Valid
    private List<ApprovalTargetRequest> referenceTargets;
    
    @Valid
    private List<@NotBlank(message = "파일 ID는 필수입니다") String> attachments;
}