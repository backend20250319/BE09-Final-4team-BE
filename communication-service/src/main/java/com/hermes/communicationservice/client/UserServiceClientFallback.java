package com.hermes.communicationservice.client;


import com.hermes.api.common.ApiResult;
import com.hermes.communicationservice.client.dto.MainProfileResponseDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResult<MainProfileResponseDto> getMainProfile(Long userId, String authorization) {
        log.error("user-service 호출 실패, fallback 실행 - userId={}", userId);

        // 실패했을 때 기본값 리턴
        MainProfileResponseDto fallbackProfile = MainProfileResponseDto.builder()
            .id(userId)
            .name("알 수 없음")
            .profileImageUrl(null)
            .build();

        return ApiResult.failure("user-service 응답 실패 (fallback)", fallbackProfile);
    }

    @Override
    public ApiResult<List<Long>> getAllUserIds(String authorization) {
        log.error("user-service getAllUserIds 호출 실패, fallback 실행");
        return ApiResult.failure("user-service 응답 실패 (getAllUserIds fallback)", List.of());
    }

}
