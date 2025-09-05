package com.hermes.orgservice.service;

import com.hermes.orgservice.dto.CreateAssignmentRequest;
import com.hermes.orgservice.dto.EmployeeAssignmentDto;
import com.hermes.orgservice.entity.EmployeeAssignment;
import com.hermes.orgservice.entity.Organization;
import com.hermes.orgservice.exception.EmployeeAssignmentNotFoundException;
import com.hermes.orgservice.exception.OrganizationNotFoundException;
import com.hermes.orgservice.repository.EmployeeAssignmentRepository;
import com.hermes.orgservice.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeAssignmentService {

    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final OrganizationRepository organizationRepository;

    public EmployeeAssignmentDto createAssignment(CreateAssignmentRequest request) {
        log.info("Creating employee assignment: employeeId={}, organizationId={}", 
                request.getEmployeeId(), request.getOrganizationId());
        
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(request.getOrganizationId()));
        
        // 중복 배정 체크 - 이미 배정되어 있으면 기존 배정 정보를 반환
        if (employeeAssignmentRepository.existsByEmployeeIdAndOrganizationOrganizationId(
                request.getEmployeeId(), request.getOrganizationId())) {
            log.info("Employee {} is already assigned to organization {}. Returning existing assignment.", 
                    request.getEmployeeId(), request.getOrganizationId());
            
            EmployeeAssignment existingAssignment = employeeAssignmentRepository
                    .findByEmployeeIdAndOrganizationOrganizationId(request.getEmployeeId(), request.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));
            
            return convertToDto(existingAssignment);
        }
        
        if (request.getIsPrimary()) {
            List<EmployeeAssignment> existingPrimaryAssignments = 
                    employeeAssignmentRepository.findByEmployeeIdAndIsPrimaryTrue(request.getEmployeeId());
            for (EmployeeAssignment assignment : existingPrimaryAssignments) {
                assignment.setIsPrimary(false);
                employeeAssignmentRepository.save(assignment);
            }
        }
        
        EmployeeAssignment assignment = EmployeeAssignment.builder()
                .employeeId(request.getEmployeeId())
                .organization(organization)
                .isPrimary(request.getIsPrimary())
                .isLeader(request.getIsLeader())
                .build();
        
        EmployeeAssignment savedAssignment = employeeAssignmentRepository.save(assignment);
        log.info("Employee assignment created: assignmentId={}", savedAssignment.getAssignmentId());
        
        return convertToDto(savedAssignment);
    }

    @Transactional(readOnly = true)
    public EmployeeAssignmentDto getAssignment(Long assignmentId) {
        EmployeeAssignment assignment = employeeAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EmployeeAssignmentNotFoundException(assignmentId));
        
        return convertToDto(assignment);
    }

    @Transactional(readOnly = true)
    public List<EmployeeAssignmentDto> getAssignmentsByEmployeeId(Long employeeId) {
        List<EmployeeAssignment> assignments = employeeAssignmentRepository.findByEmployeeId(employeeId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeAssignmentDto> getAllAssignments() {
        List<EmployeeAssignment> assignments = employeeAssignmentRepository.findAll();
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeAssignmentDto> getAssignmentsByOrganizationId(Long organizationId) {
        List<EmployeeAssignment> assignments = employeeAssignmentRepository.findByOrganizationOrganizationId(organizationId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeAssignmentDto> getPrimaryAssignmentsByEmployeeId(Long employeeId) {
        List<EmployeeAssignment> assignments = employeeAssignmentRepository.findByEmployeeIdAndIsPrimaryTrue(employeeId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeAssignmentDto> getLeadersByOrganizationId(Long organizationId) {
        List<EmployeeAssignment> assignments = employeeAssignmentRepository.findByOrganizationOrganizationIdAndIsLeaderTrue(organizationId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public EmployeeAssignmentDto updateAssignment(Long assignmentId, CreateAssignmentRequest request) {
        log.info("Updating employee assignment: assignmentId={}", assignmentId);
        
        EmployeeAssignment assignment = employeeAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EmployeeAssignmentNotFoundException(assignmentId));
        
        if (!assignment.getOrganization().getOrganizationId().equals(request.getOrganizationId())) {
            Organization newOrganization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new OrganizationNotFoundException(request.getOrganizationId()));
            assignment.setOrganization(newOrganization);
        }
        
        if (request.getIsPrimary() && !assignment.getIsPrimary()) {
            List<EmployeeAssignment> existingPrimaryAssignments = 
                    employeeAssignmentRepository.findByEmployeeIdAndIsPrimaryTrue(request.getEmployeeId());
            for (EmployeeAssignment existingAssignment : existingPrimaryAssignments) {
                existingAssignment.setIsPrimary(false);
                employeeAssignmentRepository.save(existingAssignment);
            }
        }
        
        assignment.setIsPrimary(request.getIsPrimary());
        assignment.setIsLeader(request.getIsLeader());
        
        EmployeeAssignment savedAssignment = employeeAssignmentRepository.save(assignment);
        log.info("Employee assignment updated: assignmentId={}", savedAssignment.getAssignmentId());
        
        return convertToDto(savedAssignment);
    }

    public void deleteAssignment(Long assignmentId) {
        log.info("Deleting employee assignment: assignmentId={}", assignmentId);
        
        EmployeeAssignment assignment = employeeAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EmployeeAssignmentNotFoundException(assignmentId));
        
        employeeAssignmentRepository.delete(assignment);
        log.info("Employee assignment deleted: assignmentId={}", assignmentId);
    }

    private EmployeeAssignmentDto convertToDto(EmployeeAssignment assignment) {
        return EmployeeAssignmentDto.builder()
                .assignmentId(assignment.getAssignmentId())
                .employeeId(assignment.getEmployeeId())
                .employeeName(assignment.getEmployeeName())
                .organizationId(assignment.getOrganization().getOrganizationId())
                .organizationName(assignment.getOrganization().getName())
                .isPrimary(assignment.getIsPrimary())
                .isLeader(assignment.getIsLeader())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }
}