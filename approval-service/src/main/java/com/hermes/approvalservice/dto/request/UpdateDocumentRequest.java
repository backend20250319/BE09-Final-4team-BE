package com.hermes.approvalservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateDocumentRequest {
    
    @NotBlank(message = "문서 제목은 필수입니다")
    private String title;
    
    private String content;
    
    @Valid
    private List<DocumentFieldValueRequest> fieldValues;
    
    @Valid
    private List<ApprovalStageRequest> approvalStages;
    
    @Valid
    private List<ApprovalTargetRequest> referenceTargets;
}