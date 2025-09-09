package com.hermes.attendanceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{userId}")
    Map<String, Object> getUserById(@PathVariable("userId") Long userId);
    
    @GetMapping("/api/users/count")
    Map<String, Object> getTotalEmployees();

    // 사용자의 근무정책 조회 (workPolicyId 또는 workPolicy 객체를 반환하는 사용자 서비스 엔드포인트)
    @GetMapping("/api/users/{userId}/simple")
    Map<String, Object> getUserWorkPolicy(@PathVariable("userId") Long userId);
} 