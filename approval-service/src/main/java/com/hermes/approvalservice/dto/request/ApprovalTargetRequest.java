package com.hermes.approvalservice.dto.request;

import com.hermes.approvalservice.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalTargetRequest {
    
    @NotNull(message = "대상 타입은 필수입니다")
    private TargetType targetType;
    
    private Long userId;
    
    private Long organizationId;
    
    private Integer managerLevel;
    
    @NotNull(message = "참조 여부는 필수입니다")
    private Boolean isReference;
}