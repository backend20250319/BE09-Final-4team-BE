package com.hermes.userservice.client;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.AnnualLeaveResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WorkPolicyServiceClientFallback implements WorkPolicyServiceClient {

    @Override
    public WorkPolicyResponseDto getWorkPolicy(Long id) {
        log.warn("attendance-service call failed - getWorkPolicy: {}", id);
        return null;
    }
    
    @Override
    public List<AnnualLeaveResponseDto> getAnnualLeavesByWorkPolicyId(Long workPolicyId) {
        log.warn("attendance-service call failed - getAnnualLeavesByWorkPolicyId: {}", workPolicyId);
        return List.of();
    }
}