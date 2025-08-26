package com.hermes.userservice.service;

import com.hermes.userservice.client.WorkPolicyServiceClient;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkPolicyIntegrationService {

    private final WorkPolicyServiceClient workPolicyServiceClient;
    private final UserRepository userRepository;

    public Map<String, Object> getUserWorkPolicy(Long userId) {
        try {
            // 1. 사용자 정보에서 workPolicyId 가져오기
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
            Long workPolicyId = user.getWorkPolicyId();
            
            if (workPolicyId == null) {
                return Map.of("error", "사용자에게 설정된 근무 정책이 없습니다.");
            }
            
            // 2. workPolicyId로 근무 정책 조회
            log.info("Get user work policy: userId={}, workPolicyId={}", userId, workPolicyId);
            return workPolicyServiceClient.getWorkPolicy(workPolicyId);
            
        } catch (Exception e) {
            log.error("Failed to get user work policy: userId={}, error={}", userId, e.getMessage());
            return Map.of("error", "Unable to retrieve work policy information");
        }
    }

    public Map<String, Object> getWorkPolicy(Long workPolicyId) {
        try {
            log.info("Get work policy: workPolicyId={}", workPolicyId);
            return workPolicyServiceClient.getWorkPolicy(workPolicyId);
        } catch (Exception e) {
            log.error("Failed to get work policy: workPolicyId={}, error={}", workPolicyId, e.getMessage());
            return Map.of("error", "Unable to retrieve work policy information");
        }
    }

    public Map<String, Object> createWorkPolicy(Map<String, Object> request) {
        try {
            log.info("Create work policy: request={}", request);
            return workPolicyServiceClient.createWorkPolicy(request);
        } catch (Exception e) {
            log.error("Failed to create work policy: error={}", e.getMessage());
            return Map.of("error", "Unable to create work policy");
        }
    }

    public Map<String, Object> updateWorkPolicy(Long workPolicyId, Map<String, Object> request) {
        try {
            log.info("Update work policy: workPolicyId={}, request={}", workPolicyId, request);
            return workPolicyServiceClient.updateWorkPolicy(workPolicyId, request);
        } catch (Exception e) {
            log.error("Failed to update work policy: workPolicyId={}, error={}", workPolicyId, e.getMessage());
            return Map.of("error", "Unable to update work policy");
        }
    }

    public boolean deleteWorkPolicy(Long workPolicyId) {
        try {
            log.info("Delete work policy: workPolicyId={}", workPolicyId);
            workPolicyServiceClient.deleteWorkPolicy(workPolicyId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete work policy: workPolicyId={}, error={}", workPolicyId, e.getMessage());
            return false;
        }
    }
}
