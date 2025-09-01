package com.hermes.communicationservice.client;

import com.hermes.api.common.ApiResult;
import com.hermes.communicationservice.client.dto.MainProfileResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

  // 기본 정보 가져오는 API (profileUrl 포함)
  @GetMapping("/api/users/{userId}/profile")
  ApiResult<MainProfileResponseDto> getMainProfile(@PathVariable("userId") Long userId);

}
