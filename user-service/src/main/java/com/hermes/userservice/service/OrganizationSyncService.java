package com.hermes.userservice.service;

import com.hermes.userservice.entity.User;
import com.hermes.userservice.entity.UserOrganization;
import com.hermes.userservice.exception.UserNotFoundException;
import com.hermes.userservice.repository.UserRepository;
import com.hermes.userservice.repository.UserOrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationSyncService {
    
    private final UserRepository userRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final OrganizationIntegrationService organizationIntegrationService;
    
    @Transactional
    public void syncUserOrganizations(Long userId) {
        try {
            log.info("사용자 조직 정보 동기화 시작: userId={}", userId);
            
            userOrganizationRepository.deleteAllByUserId(userId);
            
            List<Map<String, Object>> organizations = organizationIntegrationService.getUserOrganizations(userId);
            
            if (!organizations.isEmpty()) {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
                
                List<UserOrganization> userOrganizations = organizations.stream()
                    .map(org -> UserOrganization.builder()
                        .user(user)
                        .organizationId(Long.valueOf(org.get("organizationId").toString()))
                        .organizationName(org.get("organizationName").toString())
                        .isPrimary(Boolean.TRUE.equals(org.get("isPrimary")))
                        .isLeader(Boolean.TRUE.equals(org.get("isLeader")))
                        .assignedAt(LocalDateTime.parse(org.get("assignedAt").toString()))
                        .build())
                    .collect(Collectors.toList());
                
                userOrganizationRepository.saveAll(userOrganizations);
                
                log.info("사용자 조직 정보 동기화 완료: userId={}, organizationCount={}", 
                    userId, userOrganizations.size());
            } else {
                log.warn("사용자에게 배정된 조직이 없습니다: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("사용자 조직 정보 동기화 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public void syncAllUsersOrganizations() {
        log.info("전체 사용자 조직 정보 동기화 시작");
        
        List<User> users = userRepository.findAll();
        int successCount = 0;
        int failCount = 0;
        
        for (User user : users) {
            try {
                syncUserOrganizations(user.getId());
                successCount++;
            } catch (Exception e) {
                log.error("사용자 조직 정보 동기화 실패: userId={}, error={}", user.getId(), e.getMessage());
                failCount++;
            }
        }
        
        log.info("전체 사용자 조직 정보 동기화 완료: 성공={}, 실패={}", successCount, failCount);
    }
}