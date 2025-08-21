package com.hermes.authservice.service;

import com.hermes.authservice.dto.UserAuthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    public UserAuthInfo getUserByEmail(String email) {
        try {
            log.info(" [User Integration] 사용자 정보 조회 시작 - email: {}", email);

            String url = userServiceUrl + "/api/users/auth/" + email;
            UserAuthInfo userAuthInfo = restTemplate.getForObject(url, UserAuthInfo.class);

            if (userAuthInfo == null) {
                log.error(" [User Integration] 사용자 정보가 null입니다 - email: {}", email);
                throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
            }

            log.info(" [User Integration] 사용자 정보 조회 성공 - email: {}, userId: {}", email, userAuthInfo.getId());
            return userAuthInfo;

        } catch (Exception e) {
            log.error(" [User Integration] 사용자 정보 조회 실패 - email: {}, error: {}", email, e.getMessage());
            throw new RuntimeException("사용자 정보 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public void updateLastLogin(Long userId) {
        try {
            log.info(" [User Integration] 마지막 로그인 시간 업데이트 시작 - userId: {}", userId);

            String url = userServiceUrl + "/api/users/" + userId + "/last-login";
            restTemplate.postForObject(url, null, Void.class);

            log.info(" [User Integration] 마지막 로그인 시간 업데이트 성공 - userId: {}", userId);

        } catch (Exception e) {
            log.error(" [User Integration] 마지막 로그인 시간 업데이트 실패 - userId: {}, error: {}", userId, e.getMessage());
            // 로그인 시간 업데이트 실패는 로그인 자체를 실패시키지 않도록 예외를 던지지 않음
        }
    }
}