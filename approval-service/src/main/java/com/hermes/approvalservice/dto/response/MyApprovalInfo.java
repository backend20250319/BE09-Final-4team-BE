package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.enums.ApprovalStatus;
import lombok.Data;

@Data
public class MyApprovalInfo {
    
    private ApprovalStatus myApprovalStatus;
    private Boolean isApprovalRequired;
    private Integer myApprovalStage;
}