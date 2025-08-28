package com.hermes.userservice.client;

import com.hermes.api.common.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "org-service", fallback = OrgServiceClientFallback.class)
public interface OrgServiceClient {

    @GetMapping("/api/organizations/{organizationId}")
    ApiResult<Map<String, Object>> getOrganization(@PathVariable("organizationId") Long organizationId);

    @GetMapping("/api/organizations")
    ApiResult<List<Map<String, Object>>> getAllOrganizations();

    @GetMapping("/api/organizations/root")
    ApiResult<List<Map<String, Object>>> getRootOrganizations();

    @GetMapping("/api/organizations/hierarchy")
    ApiResult<List<Map<String, Object>>> getOrganizationHierarchy();

    @GetMapping("/api/assignments/employee/{employeeId}")
    ApiResult<List<Map<String, Object>>> getAssignmentsByEmployeeId(@PathVariable("employeeId") Long employeeId);

    @GetMapping("/api/assignments")
    ApiResult<List<Map<String, Object>>> getAllAssignments();

    @GetMapping("/api/assignments/employee/{employeeId}/primary")
    ApiResult<List<Map<String, Object>>> getPrimaryAssignmentsByEmployeeId(@PathVariable("employeeId") Long employeeId);

    @GetMapping("/api/assignments/organization/{organizationId}")
    ApiResult<List<Map<String, Object>>> getAssignmentsByOrganizationId(@PathVariable("organizationId") Long organizationId);

    @GetMapping("/api/assignments/organization/{organizationId}/leaders")
    ApiResult<List<Map<String, Object>>> getLeadersByOrganizationId(@PathVariable("organizationId") Long organizationId);

    @PostMapping("/api/organizations")
    ApiResult<Map<String, Object>> createOrganization(@RequestBody Map<String, Object> request);

    @PutMapping("/api/organizations/{organizationId}")
    ApiResult<Map<String, Object>> updateOrganization(@PathVariable("organizationId") Long organizationId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/api/organizations/{organizationId}")
    void deleteOrganization(@PathVariable("organizationId") Long organizationId);

    @PostMapping("/api/assignments")
    ApiResult<Map<String, Object>> createAssignment(@RequestBody Map<String, Object> request);

    @PutMapping("/api/assignments/{assignmentId}")
    ApiResult<Map<String, Object>> updateAssignment(@PathVariable("assignmentId") Long assignmentId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/api/assignments/{assignmentId}")
    void deleteAssignment(@PathVariable("assignmentId") Long assignmentId);

    @DeleteMapping("/api/assignments/employee/{employeeId}")
    void deleteAssignmentsByEmployeeId(@PathVariable("employeeId") Long employeeId);
}