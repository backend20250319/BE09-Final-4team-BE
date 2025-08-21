package com.hermes.orgservice.controller;

import com.hermes.orgservice.dto.CreateAssignmentRequest;
import com.hermes.orgservice.dto.EmployeeAssignmentDto;
import com.hermes.orgservice.service.EmployeeAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class EmployeeAssignmentController {

    private final EmployeeAssignmentService employeeAssignmentService;

    @PostMapping
    public ResponseEntity<EmployeeAssignmentDto> createAssignment(@Valid @RequestBody CreateAssignmentRequest request) {
        log.info("Create employee assignment API called: employeeId={}, organizationId={}", 
                request.getEmployeeId(), request.getOrganizationId());
        EmployeeAssignmentDto createdAssignment = employeeAssignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<EmployeeAssignmentDto> getAssignment(@PathVariable Long assignmentId) {
        log.info("Get employee assignment API called: assignmentId={}", assignmentId);
        EmployeeAssignmentDto assignment = employeeAssignmentService.getAssignment(assignmentId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeAssignmentDto>> getAssignmentsByEmployeeId(@PathVariable Long employeeId) {
        log.info("Get employee assignments API called: employeeId={}", employeeId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<EmployeeAssignmentDto>> getAssignmentsByOrganizationId(@PathVariable Long organizationId) {
        log.info("Get organization assignments API called: organizationId={}", organizationId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getAssignmentsByOrganizationId(organizationId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/employee/{employeeId}/primary")
    public ResponseEntity<List<EmployeeAssignmentDto>> getPrimaryAssignmentsByEmployeeId(@PathVariable Long employeeId) {
        log.info("Get primary assignments API called: employeeId={}", employeeId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getPrimaryAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/organization/{organizationId}/leaders")
    public ResponseEntity<List<EmployeeAssignmentDto>> getLeadersByOrganizationId(@PathVariable Long organizationId) {
        log.info("Get organization leaders API called: organizationId={}", organizationId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getLeadersByOrganizationId(organizationId);
        return ResponseEntity.ok(assignments);
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<EmployeeAssignmentDto> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody CreateAssignmentRequest request) {
        log.info("Update employee assignment API called: assignmentId={}", assignmentId);
        EmployeeAssignmentDto updatedAssignment = employeeAssignmentService.updateAssignment(assignmentId, request);
        return ResponseEntity.ok(updatedAssignment);
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        log.info("Delete employee assignment API called: assignmentId={}", assignmentId);
        employeeAssignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
