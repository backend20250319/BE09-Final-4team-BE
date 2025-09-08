package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.enums.ApprovalStatus;
import com.hermes.approvalservice.enums.TargetType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalTargetResponse {
    
    private Long id;
    private TargetType targetType;
    private UserProfile user;
    private Long organizationId;
    private Integer managerLevel;
    private Boolean isReference;
    private ApprovalStatus approvalStatus;
    private UserProfile processor;
    private LocalDateTime processedAt;
}