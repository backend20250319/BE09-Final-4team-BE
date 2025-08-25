package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovalStageResponse {
    
    private Long id;
    private Integer stageOrder;
    private String stageName;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private List<ApprovalTargetResponse> approvalTargets;
}