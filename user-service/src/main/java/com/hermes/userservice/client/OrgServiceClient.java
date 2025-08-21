package com.hermes.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "org-service", fallback = OrgServiceClientFallback.class)
public interface OrgServiceClient {

    @GetMapping("/api/organizations/{organizationId}")
    Map<String, Object> getOrganization(@PathVariable("organizationId") Long organizationId);

    @GetMapping("/api/organizations")
    List<Map<String, Object>> getAllOrganizations();

    @GetMapping("/api/organizations/root")
    List<Map<String, Object>> getRootOrganizations();

    @GetMapping("/api/organizations/hierarchy")
    List<Map<String, Object>> getOrganizationHierarchy();

    @GetMapping("/api/assignments/employee/{employeeId}")
    List<Map<String, Object>> getAssignmentsByEmployeeId(@PathVariable("employeeId") Long employeeId);

    @GetMapping("/api/assignments/employee/{employeeId}/primary")
    List<Map<String, Object>> getPrimaryAssignmentsByEmployeeId(@PathVariable("employeeId") Long employeeId);

    @GetMapping("/api/assignments/organization/{organizationId}")
    List<Map<String, Object>> getAssignmentsByOrganizationId(@PathVariable("organizationId") Long organizationId);

    @GetMapping("/api/assignments/organization/{organizationId}/leaders")
    List<Map<String, Object>> getLeadersByOrganizationId(@PathVariable("organizationId") Long organizationId);
}