package com.hermes.userservice.client;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkPolicyServiceClientFallback implements WorkPolicyServiceClient {

    @Override
    public WorkPolicyResponseDto getWorkPolicy(Long id) {
        log.warn("workpolicy-service call failed - getWorkPolicy: {}", id);
        return WorkPolicyResponseDto.builder()
                .id(id)
                .name("Service Unavailable")
                .build();
    }

    @Override
    public WorkPolicyResponseDto createWorkPolicy(WorkPolicyRequestDto request) {
        log.warn("workpolicy-service call failed - createWorkPolicy");
        return WorkPolicyResponseDto.builder()
                .name("Service Unavailable")
                .build();
    }

    @Override
    public WorkPolicyResponseDto updateWorkPolicy(Long id, WorkPolicyUpdateDto request) {
        log.warn("workpolicy-service call failed - updateWorkPolicy: {}", id);
        return WorkPolicyResponseDto.builder()
                .id(id)
                .name("Service Unavailable")
                .build();
    }

    @Override
    public void deleteWorkPolicy(Long id) {
        log.warn("workpolicy-service call failed - deleteWorkPolicy: {}", id);
    }
}