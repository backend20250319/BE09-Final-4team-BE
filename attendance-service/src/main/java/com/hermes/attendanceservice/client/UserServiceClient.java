package com.hermes.attendanceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{userId}/simple")
    Map<String, Object> getUserById(@PathVariable("userId") Long userId);
    
    @GetMapping("/api/users/{userId}")
    Map<String, Object> getUserById(@PathVariable("userId") Long userId, @RequestHeader(value = "Authorization", required = false) String authorization);
    
    @GetMapping("/api/users/count")
    Map<String, Object> getTotalEmployees();

} 