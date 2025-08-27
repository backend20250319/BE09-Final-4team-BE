package com.hermes.userservice.client;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyUpdateDto;
import com.hermes.api.common.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "attendance-service", fallback = WorkPolicyServiceClientFallback.class)
public interface WorkPolicyServiceClient {

    @GetMapping("/api/work-policies/{id}")
    ApiResult<WorkPolicyResponseDto> getWorkPolicy(@PathVariable("id") Long id);

    @PostMapping("/api/work-policies")
    ApiResult<WorkPolicyResponseDto> createWorkPolicy(@RequestBody WorkPolicyRequestDto request);

    @PutMapping("/api/work-policies/{id}")
    ApiResult<WorkPolicyResponseDto> updateWorkPolicy(@PathVariable("id") Long id, @RequestBody WorkPolicyUpdateDto request);

    @DeleteMapping("/api/work-policies/{id}")
    ApiResult<Void> deleteWorkPolicy(@PathVariable("id") Long id);
}