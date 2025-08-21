package com.hermes.approvalservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ApprovalStageRequest {
    
    @NotNull(message = "단계 순서는 필수입니다")
    private Integer stageOrder;
    
    @NotBlank(message = "단계명은 필수입니다")
    private String stageName;
    
    @Valid
    private List<ApprovalTargetRequest> approvalTargets;
}