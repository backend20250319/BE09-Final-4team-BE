package com.hermes.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "workpolicy-service", fallback = WorkPolicyServiceClientFallback.class)
public interface WorkPolicyServiceClient {

    @GetMapping("/api/v1/work-policies/{id}")
    Map<String, Object> getWorkPolicy(@PathVariable("id") Long id);

    @PostMapping("/api/v1/work-policies")
    Map<String, Object> createWorkPolicy(@RequestBody Map<String, Object> request);

    @PutMapping("/api/v1/work-policies/{id}")
    Map<String, Object> updateWorkPolicy(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @DeleteMapping("/api/v1/work-policies/{id}")
    void deleteWorkPolicy(@PathVariable("id") Long id);
}
