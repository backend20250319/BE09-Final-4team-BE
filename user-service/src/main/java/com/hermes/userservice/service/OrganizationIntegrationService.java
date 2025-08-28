package com.hermes.userservice.service;

import com.hermes.api.common.ApiResult;
import com.hermes.userservice.client.OrgServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationIntegrationService {

    private final OrgServiceClient orgServiceClient;

    public List<Map<String, Object>> getUserOrganizations(Long userId) {
        try {
            log.info("Get user organizations: userId={}", userId);
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getAssignmentsByEmployeeId(userId);

            if ("SUCCESS".equals(apiResult.getStatus())) {
                List<Map<String, Object>> result = apiResult.getData();
                log.info("Get user organizations result: userId={}, count={}", userId, result.size());
                return result;
            } else {
                log.error("Failed to get user organizations from org-service: {}", apiResult.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for user organizations: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    public Map<Long, List<Map<String, Object>>> getAllUsersOrganizations() {
        try {
            log.info("Get all users organizations");
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getAllAssignments();

            if ("SUCCESS".equals(apiResult.getStatus())) {
                List<Map<String, Object>> allAssignments = apiResult.getData();
                Map<Long, List<Map<String, Object>>> groupedByUserId = allAssignments.stream()
                        .collect(Collectors.groupingBy(
                                assignment -> ((Number) assignment.get("employeeId")).longValue()
                        ));
                log.info("Get all users organizations result: userCount={}", groupedByUserId.size());
                return groupedByUserId;
            } else {
                log.error("Failed to get all assignments from org-service: {}", apiResult.getMessage());
                return Map.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for all assignments: error={}", e.getMessage(), e);
            return Map.of();
        }
    }

    public List<Map<String, Object>> getUserPrimaryOrganization(Long userId) {
        try {
            log.info("Get user primary organization: userId={}", userId);
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getPrimaryAssignmentsByEmployeeId(userId);
            
            if ("SUCCESS".equals(apiResult.getStatus())) {
                return apiResult.getData();
            } else {
                log.error("Failed to get user primary organization from org-service: {}", apiResult.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for user primary organization: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> getOrganization(Long organizationId) {
        try {
            log.info("Get organization: organizationId={}", organizationId);
            ApiResult<Map<String, Object>> apiResult = orgServiceClient.getOrganization(organizationId);
            
            if ("SUCCESS".equals(apiResult.getStatus())) {
                return apiResult.getData();
            } else {
                log.error("Failed to get organization from org-service: {}", apiResult.getMessage());
                return Map.of("error", "Unable to retrieve organization information");
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for organization: organizationId={}, error={}", organizationId, e.getMessage());
            return Map.of("error", "Unable to retrieve organization information");
        }
    }

    public List<Map<String, Object>> getAllOrganizations() {
        try {
            log.info("Get all organizations");
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getAllOrganizations();
            
            if ("SUCCESS".equals(apiResult.getStatus())) {
                return apiResult.getData();
            } else {
                log.error("Failed to get all organizations from org-service: {}", apiResult.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for all organizations: error={}", e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getRootOrganizations() {
        try {
            log.info("Get root organizations");
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getRootOrganizations();
            
            if ("SUCCESS".equals(apiResult.getStatus())) {
                return apiResult.getData();
            } else {
                log.error("Failed to get root organizations from org-service: {}", apiResult.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for root organizations: error={}", e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getOrganizationHierarchy() {
        try {
            log.info("Get organization hierarchy");
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getOrganizationHierarchy();
            
            if ("SUCCESS".equals(apiResult.getStatus())) {
                return apiResult.getData();
            } else {
                log.error("Failed to get organization hierarchy from org-service: {}", apiResult.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for organization hierarchy: error={}", e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getOrganizationMembers(Long organizationId) {
        try {
            log.info("Get organization members: organizationId={}", organizationId);
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getAssignmentsByOrganizationId(organizationId);
            
            if ("SUCCESS".equals(apiResult.getStatus())) {
                return apiResult.getData();
            } else {
                log.error("Failed to get organization members from org-service: {}", apiResult.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for organization members: organizationId={}, error={}", organizationId, e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getOrganizationLeaders(Long organizationId) {
        try {
            log.info("Get organization leaders: organizationId={}", organizationId);
            ApiResult<List<Map<String, Object>>> apiResult = orgServiceClient.getLeadersByOrganizationId(organizationId);
            
            if ("SUCCESS".equals(apiResult.getStatus())) {
                return apiResult.getData();
            } else {
                log.error("Failed to get organization leaders from org-service: {}", apiResult.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to call org-service for organization leaders: organizationId={}, error={}", organizationId, e.getMessage());
            return List.of();
        }
    }
}
