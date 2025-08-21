package com.hermes.userservice.service;

import com.hermes.userservice.client.OrgServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationIntegrationService {

    private final OrgServiceClient orgServiceClient;

    public List<Map<String, Object>> getUserOrganizations(Long userId) {
        try {
            log.info("Get user organizations: userId={}", userId);
            return orgServiceClient.getAssignmentsByEmployeeId(userId);
        } catch (Exception e) {
            log.error("Failed to get user organizations: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getUserPrimaryOrganization(Long userId) {
        try {
            log.info("Get user primary organization: userId={}", userId);
            return orgServiceClient.getPrimaryAssignmentsByEmployeeId(userId);
        } catch (Exception e) {
            log.error("Failed to get user primary organization: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> getOrganization(Long organizationId) {
        try {
            log.info("Get organization: organizationId={}", organizationId);
            return orgServiceClient.getOrganization(organizationId);
        } catch (Exception e) {
            log.error("Failed to get organization: organizationId={}, error={}", organizationId, e.getMessage());
            return Map.of("error", "Unable to retrieve organization information");
        }
    }

    public List<Map<String, Object>> getAllOrganizations() {
        try {
            log.info("Get all organizations");
            return orgServiceClient.getAllOrganizations();
        } catch (Exception e) {
            log.error("Failed to get all organizations: error={}", e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getRootOrganizations() {
        try {
            log.info("Get root organizations");
            return orgServiceClient.getRootOrganizations();
        } catch (Exception e) {
            log.error("Failed to get root organizations: error={}", e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getOrganizationHierarchy() {
        try {
            log.info("Get organization hierarchy");
            return orgServiceClient.getOrganizationHierarchy();
        } catch (Exception e) {
            log.error("Failed to get organization hierarchy: error={}", e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getOrganizationMembers(Long organizationId) {
        try {
            log.info("Get organization members: organizationId={}", organizationId);
            return orgServiceClient.getAssignmentsByOrganizationId(organizationId);
        } catch (Exception e) {
            log.error("Failed to get organization members: organizationId={}, error={}", organizationId, e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> getOrganizationLeaders(Long organizationId) {
        try {
            log.info("Get organization leaders: organizationId={}", organizationId);
            return orgServiceClient.getLeadersByOrganizationId(organizationId);
        } catch (Exception e) {
            log.error("Failed to get organization leaders: organizationId={}, error={}", organizationId, e.getMessage());
            return List.of();
        }
    }
}
