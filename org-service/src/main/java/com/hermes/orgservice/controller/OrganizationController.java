package com.hermes.orgservice.controller;

import com.hermes.orgservice.dto.CreateOrganizationRequest;
import com.hermes.orgservice.dto.OrganizationDto;
import com.hermes.orgservice.dto.OrganizationHierarchyDto;
import com.hermes.orgservice.dto.UpdateOrganizationRequest;
import com.hermes.orgservice.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationDto> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        log.info("Create organization API called: {}", request.getName());
        OrganizationDto createdOrganization = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrganization);
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable Long organizationId) {
        log.info("Get organization API called: organizationId={}", organizationId);
        OrganizationDto organization = organizationService.getOrganization(organizationId);
        return ResponseEntity.ok(organization);
    }

    @GetMapping("/root")
    public ResponseEntity<List<OrganizationDto>> getRootOrganizations() {
        log.info("Get root organizations API called");
        List<OrganizationDto> organizations = organizationService.getRootOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        log.info("Get all organizations API called");
        List<OrganizationDto> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<List<OrganizationHierarchyDto>> getOrganizationHierarchy() {
        log.info("Get organization hierarchy API called");
        List<OrganizationHierarchyDto> hierarchy = organizationService.getOrganizationHierarchy();
        return ResponseEntity.ok(hierarchy);
    }

    @PutMapping("/{organizationId}")
    public ResponseEntity<OrganizationDto> updateOrganization(
            @PathVariable Long organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        log.info("Update organization API called: organizationId={}", organizationId);
        OrganizationDto updatedOrganization = organizationService.updateOrganization(organizationId, request);
        return ResponseEntity.ok(updatedOrganization);
    }

    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long organizationId) {
        log.info("Delete organization API called: organizationId={}", organizationId);
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrganizationDto>> searchOrganizations(@RequestParam String keyword) {
        log.info("Search organizations API called: keyword={}", keyword);
        List<OrganizationDto> organizations = organizationService.searchOrganizations(keyword);
        return ResponseEntity.ok(organizations);
    }
}
