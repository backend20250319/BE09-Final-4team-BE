package com.hermes.userservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WorkPolicyServiceClientFallback implements WorkPolicyServiceClient {

    @Override
    public Map<String, Object> getWorkPolicy(Long id) {
        log.warn("workpolicy-service call failed - getWorkPolicy: {}", id);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("error", "workpolicy-service connection failed");
        return fallback;
    }

    @Override
    public Map<String, Object> createWorkPolicy(Map<String, Object> request) {
        log.warn("workpolicy-service call failed - createWorkPolicy");
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "workpolicy-service connection failed");
        return fallback;
    }

    @Override
    public Map<String, Object> updateWorkPolicy(Long id, Map<String, Object> request) {
        log.warn("workpolicy-service call failed - updateWorkPolicy: {}", id);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("error", "workpolicy-service connection failed");
        return fallback;
    }

    @Override
    public void deleteWorkPolicy(Long id) {
        log.warn("workpolicy-service call failed - deleteWorkPolicy: {}", id);
    }
}
