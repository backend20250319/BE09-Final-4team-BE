package com.hermes.attendanceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{userId}")
    Map<String, Object> getUserByIdRaw(@PathVariable("userId") Long userId);
    
    @GetMapping("/api/users/count")
    Map<String, Object> getTotalEmployeesRaw();

    @GetMapping("/api/users/{userId}/simple")
    Map<String, Object> getUserWorkPolicyRaw(@PathVariable("userId") Long userId);

    default Map<String, Object> getUserById(Long userId) {
        Map<String, Object> wrapper = getUserByIdRaw(userId);
        Object data = wrapper != null ? wrapper.get("data") : null;
        return data instanceof Map ? (Map<String, Object>) data : Map.of();
    }

    default Map<String, Object> getTotalEmployees() {
        Map<String, Object> wrapper = getTotalEmployeesRaw();
        Object data = wrapper != null ? wrapper.get("data") : null;
        return data instanceof Map ? (Map<String, Object>) data : Map.of();
    }

    default Map<String, Object> getUserWorkPolicy(Long userId) {
        Map<String, Object> wrapper = getUserWorkPolicyRaw(userId);
        Object data = wrapper != null ? wrapper.get("data") : null;
        return data instanceof Map ? (Map<String, Object>) data : Map.of();
    }
} 