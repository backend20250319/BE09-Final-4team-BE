package com.hermes.approvalservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateTemplateRequest {
    
    @NotBlank(message = "양식 제목은 필수입니다")
    private String title;
    
    private String icon;
    
    private String description;
    
    private String bodyTemplate;
    
    @NotNull(message = "본문 사용 여부는 필수입니다")
    private Boolean useBody;
    
    @NotNull(message = "첨부파일 사용 여부는 필수입니다")
    private Boolean useAttachment;
    
    @NotNull(message = "승인대상 변경 허용 여부는 필수입니다")
    private Boolean allowApprovalChange;
    
    @NotNull(message = "참조대상 변경 허용 여부는 필수입니다")
    private Boolean allowReferenceChange;
    
    private String referenceFiles;
    
    private Long categoryId;
    
    @Valid
    private List<TemplateFieldRequest> fields;
    
    @Valid
    private List<ApprovalStageRequest> approvalStages;
    
    @Valid
    private List<ApprovalTargetRequest> referenceTargets;
}