package com.hermes.orgservice.service;

import com.hermes.orgservice.dto.CreateOrganizationRequest;
import com.hermes.orgservice.dto.OrganizationDto;
import com.hermes.orgservice.dto.OrganizationHierarchyDto;
import com.hermes.orgservice.dto.UpdateOrganizationRequest;
import com.hermes.orgservice.entity.Organization;
import com.hermes.orgservice.exception.DuplicateOrganizationException;
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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;

    public OrganizationDto createOrganization(CreateOrganizationRequest request) {
        log.info("Organization creation requested: {}", request.getName());
        
        if (organizationRepository.existsByName(request.getName())) {
            throw new DuplicateOrganizationException("Organization name already exists: " + request.getName());
        }
        
        Organization parent = null;
        if (request.getParentId() != null) {
            parent = organizationRepository.findById(request.getParentId())
                    .orElseThrow(() -> new OrganizationNotFoundException(request.getParentId()));
        }
        
        Organization organization = Organization.builder()
                .name(request.getName())
                .parent(parent)
                .build();
        
        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization created successfully: {}", savedOrganization.getName());
        
        return convertToDto(savedOrganization);
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        
        return convertToDto(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getRootOrganizations() {
        List<Organization> rootOrganizations = organizationRepository.findByParentIsNull();
        return rootOrganizations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getAllOrganizations() {
        List<Organization> organizations = organizationRepository.findAll();
        return organizations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public OrganizationDto updateOrganization(Long organizationId, UpdateOrganizationRequest request) {
        log.info("Organization update requested: ID={}, name={}", organizationId, request.getName());
        
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        
        if (!organization.getName().equals(request.getName()) && 
            organizationRepository.existsByName(request.getName())) {
            throw new DuplicateOrganizationException("Organization name already exists: " + request.getName());
        }
        
        organization.setName(request.getName());
        
        if (request.getParentId() != null) {
            Organization parent = organizationRepository.findById(request.getParentId())
                    .orElseThrow(() -> new OrganizationNotFoundException(request.getParentId()));
            organization.setParent(parent);
        } else {
            organization.setParent(null);
        }
        
        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization updated successfully: {}", savedOrganization.getName());
        
        return convertToDto(savedOrganization);
    }

    public void deleteOrganization(Long organizationId) {
        log.info("Organization deletion requested: ID={}", organizationId);
        
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        
        if (!organization.getChildren().isEmpty()) {
            throw new RuntimeException("Cannot delete organization with child organizations.");
        }
        
        long memberCount = employeeAssignmentRepository.countByOrganizationOrganizationId(organizationId);
        if (memberCount > 0) {
            throw new RuntimeException("Cannot delete organization with assigned employees.");
        }
        
        organizationRepository.delete(organization);
        log.info("Organization deleted successfully: {}", organization.getName());
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> searchOrganizations(String keyword) {
        List<Organization> organizations = organizationRepository.findByNameContaining(keyword);
        return organizations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationHierarchyDto> getOrganizationHierarchy() {
        log.info("Organization hierarchy requested");
        List<Organization> rootOrganizations = organizationRepository.findByParentIsNull(); // 수정
        return rootOrganizations.stream()
                .map(this::convertToHierarchyDto)
                .collect(Collectors.toList());
    }


    private OrganizationDto convertToDto(Organization organization) {
        long memberCount = employeeAssignmentRepository.countByOrganizationOrganizationId(organization.getOrganizationId());
        long leaderCount = employeeAssignmentRepository.countByOrganizationOrganizationIdAndIsLeaderTrue(organization.getOrganizationId());
        
        return OrganizationDto.builder()
                .organizationId(organization.getOrganizationId())
                .name(organization.getName())
                .parentId(organization.getParent() != null ? organization.getParent().getOrganizationId() : null)
                .parentName(organization.getParent() != null ? organization.getParent().getName() : null)
                .memberCount((int) memberCount)
                .leaderCount((int) leaderCount)
                .build();
    }

    private OrganizationHierarchyDto convertToHierarchyDto(Organization organization) {
        long memberCount = employeeAssignmentRepository.countByOrganizationOrganizationId(organization.getOrganizationId());
        long leaderCount = employeeAssignmentRepository.countByOrganizationOrganizationIdAndIsLeaderTrue(organization.getOrganizationId());
        
        List<OrganizationHierarchyDto> children = null;
        if (organization.getChildren() != null && !organization.getChildren().isEmpty()) {
            children = organization.getChildren().stream()
                    .map(this::convertToHierarchyDto)
                    .collect(Collectors.toList());
        }
        
        return OrganizationHierarchyDto.builder()
                .organizationId(organization.getOrganizationId())
                .name(organization.getName())
                .parentId(organization.getParent() != null ? organization.getParent().getOrganizationId() : null)
                .parentName(organization.getParent() != null ? organization.getParent().getName() : null)
                .children(children)
                .memberCount((int) memberCount)
                .leaderCount((int) leaderCount)
                .isExpanded(false)
                .build();
    }
}
