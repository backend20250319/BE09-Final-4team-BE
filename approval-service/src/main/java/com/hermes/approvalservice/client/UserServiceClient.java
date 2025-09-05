package com.hermes.approvalservice.client;

import com.hermes.api.common.ApiResult;
import com.hermes.approvalservice.client.dto.UserProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}/profile")
    ApiResult<UserProfile> getUserProfile(@PathVariable("userId") Long userId);

    @GetMapping("/api/users/search-ids")
    List<Long> searchUserIds(@RequestParam("name") String name);

}