package com.hermes.workscheduleservice.client;

import com.hermes.workscheduleservice.dto.WorkPolicyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "attendance-service", url = "${attendance-service.url:http://localhost:8082}")
public interface AttendanceServiceClient {
    
    @GetMapping("/api/workpolicy/{workPolicyId}")
    WorkPolicyDto getWorkPolicyById(@PathVariable("workPolicyId") Long workPolicyId);
}