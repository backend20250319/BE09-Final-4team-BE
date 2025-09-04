package com.hermes.approvalservice.client;

import com.hermes.api.common.ApiResult;
import com.hermes.approvalservice.client.dto.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResult<UserProfile> getUserProfile(Long userId) {
        log.error("UserServiceClient fallback triggered for getUserProfile, userId: {}", userId);
        
        UserProfile fallbackUserProfile = UserProfile.builder()
                .id(userId)
                .name("사용자 정보 없음")
                .email("")
                .phone("")
                .profileImageUrl("")
                .build();
        
        return ApiResult.success(fallbackUserProfile);
    }
}