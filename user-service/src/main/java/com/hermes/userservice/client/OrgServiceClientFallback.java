package com.hermes.userservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OrgServiceClientFallback implements OrgServiceClient {

    @Override
    public Map<String, Object> getOrganization(Long organizationId) {
        log.warn("org-service call failed - getOrganization: {}", organizationId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("organizationId", organizationId);
        fallback.put("name", "Unable to retrieve organization information");
        fallback.put("error", "org-service connection failed");
        return fallback;
    }

    @Override
    public List<Map<String, Object>> getAllOrganizations() {
        log.warn("org-service call failed - getAllOrganizations");
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getRootOrganizations() {
        log.warn("org-service call failed - getRootOrganizations");
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getOrganizationHierarchy() {
        log.warn("org-service call failed - getOrganizationHierarchy");
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getAssignmentsByEmployeeId(Long employeeId) {
        log.warn("org-service call failed - getAssignmentsByEmployeeId: {}", employeeId);
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getPrimaryAssignmentsByEmployeeId(Long employeeId) {
        log.warn("org-service call failed - getPrimaryAssignmentsByEmployeeId: {}", employeeId);
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getAssignmentsByOrganizationId(Long organizationId) {
        log.warn("org-service call failed - getAssignmentsByOrganizationId: {}", organizationId);
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getLeadersByOrganizationId(Long organizationId) {
        log.warn("org-service call failed - getLeadersByOrganizationId: {}", organizationId);
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> createOrganization(Map<String, Object> request) {
        log.warn("org-service call failed - createOrganization");
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "org-service connection failed");
        return fallback;
    }

    @Override
    public Map<String, Object> updateOrganization(Long organizationId, Map<String, Object> request) {
        log.warn("org-service call failed - updateOrganization: {}", organizationId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("organizationId", organizationId);
        fallback.put("error", "org-service connection failed");
        return fallback;
    }

    @Override
    public void deleteOrganization(Long organizationId) {
        log.warn("org-service call failed - deleteOrganization: {}", organizationId);
    }

    @Override
    public Map<String, Object> createAssignment(Map<String, Object> request) {
        log.warn("org-service call failed - createAssignment");
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "org-service connection failed");
        return fallback;
    }

    @Override
    public Map<String, Object> updateAssignment(Long assignmentId, Map<String, Object> request) {
        log.warn("org-service call failed - updateAssignment: {}", assignmentId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("assignmentId", assignmentId);
        fallback.put("error", "org-service connection failed");
        return fallback;
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        log.warn("org-service call failed - deleteAssignment: {}", assignmentId);
    }

    @Override
    public void deleteAssignmentsByEmployeeId(Long employeeId) {
        log.warn("org-service call failed - deleteAssignmentsByEmployeeId: {}", employeeId);
    }
}
