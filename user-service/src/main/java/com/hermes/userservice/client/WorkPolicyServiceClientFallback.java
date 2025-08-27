package com.hermes.userservice.client;

import com.hermes.api.common.ApiResult;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkPolicyServiceClientFallback implements WorkPolicyServiceClient {

    @Override
    public ApiResult<WorkPolicyResponseDto> getWorkPolicy(Long id) {
        log.warn("attendance-service call failed - getWorkPolicy: {}", id);
        return ApiResult.failure("Service Unavailable");
    }
}