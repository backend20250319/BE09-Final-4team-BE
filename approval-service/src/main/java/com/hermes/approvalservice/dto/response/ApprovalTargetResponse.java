package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.enums.TargetType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalTargetResponse {
    
    private Long id;
    private TargetType targetType;
    private Long userId;
    private Long organizationId;
    private Integer managerLevel;
    private Boolean isReference;
    private Boolean isApproved;
    private Long approvedBy;
    private LocalDateTime approvedAt;
}