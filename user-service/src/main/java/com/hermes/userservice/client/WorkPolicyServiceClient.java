package com.hermes.userservice.client;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.dto.workpolicy.AnnualLeaveResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "attendance-service", fallback = WorkPolicyServiceClientFallback.class)
public interface WorkPolicyServiceClient {

    @GetMapping("/api/workpolicy/{id}")
    WorkPolicyResponseDto getWorkPolicy(@PathVariable("id") Long id);
    
    @GetMapping("/api/annual-leaves/work-policies/{workPolicyId}")
    List<AnnualLeaveResponseDto> getAnnualLeavesByWorkPolicyId(@PathVariable("workPolicyId") Long workPolicyId);
}