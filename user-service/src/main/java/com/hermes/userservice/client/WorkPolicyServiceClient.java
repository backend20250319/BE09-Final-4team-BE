package com.hermes.userservice.client;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyUpdateDto;
import com.hermes.userservice.dto.workpolicy.AnnualLeaveResponseDto;
import com.hermes.api.common.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "attendance-service", fallback = WorkPolicyServiceClientFallback.class)
public interface WorkPolicyServiceClient {

    @GetMapping("/api/workpolicy/{id}")
    ApiResult<WorkPolicyResponseDto> getWorkPolicy(@PathVariable("id") Long id);
    
    @GetMapping("/api/annual-leaves/work-policies/{workPolicyId}")
    ApiResult<List<AnnualLeaveResponseDto>> getAnnualLeavesByWorkPolicyId(@PathVariable("workPolicyId") Long workPolicyId);
}