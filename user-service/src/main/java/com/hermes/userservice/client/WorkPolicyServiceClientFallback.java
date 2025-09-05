package com.hermes.userservice.client;

import com.hermes.api.common.ApiResult;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyUpdateDto;
import com.hermes.userservice.dto.workpolicy.AnnualLeaveResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WorkPolicyServiceClientFallback implements WorkPolicyServiceClient {

    @Override
    public ApiResult<WorkPolicyResponseDto> getWorkPolicy(Long id) {
        log.warn("attendance-service call failed - getWorkPolicy: {}", id);
        return ApiResult.failure("Service Unavailable");
    }
    
    @Override
    public ApiResult<List<AnnualLeaveResponseDto>> getAnnualLeavesByWorkPolicyId(Long workPolicyId) {
        log.warn("attendance-service call failed - getAnnualLeavesByWorkPolicyId: {}", workPolicyId);
        return ApiResult.failure("Service Unavailable");
    }
}