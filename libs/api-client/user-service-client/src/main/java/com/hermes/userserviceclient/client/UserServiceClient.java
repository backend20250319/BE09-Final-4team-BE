package com.hermes.userserviceclient.client;

import com.hermes.api.common.ApiResult;
import com.hermes.userserviceclient.dto.UserDetailResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{userId}")
    ResponseEntity<ApiResult<UserDetailResponseDto>> getUserDetail(@PathVariable("userId") Long userId);
} 