package com.hermes.approvalservice.dto.request;

import com.hermes.approvalservice.enums.AttachmentUsageType;
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
    
    @NotNull(message = "첨부파일 사용 유형은 필수입니다")
    private AttachmentUsageType useAttachment;
    
    @NotNull(message = "대상 변경 허용 여부는 필수입니다")
    private Boolean allowTargetChange;
    
    @Valid
    private List<@NotBlank(message = "파일 ID는 필수입니다") String> referenceFiles;
    
    private Long categoryId;
    
    @Valid
    private List<TemplateFieldRequest> fields;
    
    @Valid
    private List<ApprovalStageRequest> approvalStages;
    
    @Valid
    private List<ApprovalTargetRequest> referenceTargets;
}