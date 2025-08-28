package com.hermes.orgservice.controller;

import com.hermes.api.common.ApiResult;
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
    public ResponseEntity<ApiResult<OrganizationDto>> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        log.info("Create organization API called: {}", request.getName());
        OrganizationDto createdOrganization = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("조직 생성 성공", createdOrganization));
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResult<OrganizationDto>> getOrganization(@PathVariable Long organizationId) {
        log.info("Get organization API called: organizationId={}", organizationId);
        OrganizationDto organization = organizationService.getOrganization(organizationId);
        return ResponseEntity.ok(ApiResult.success("조직 정보 조회 성공", organization));
    }

    @GetMapping("/root")
    public ResponseEntity<ApiResult<List<OrganizationDto>>> getRootOrganizations() {
        log.info("Get root organizations API called");
        List<OrganizationDto> organizations = organizationService.getRootOrganizations();
        return ResponseEntity.ok(ApiResult.success("최상위 조직 목록 조회 성공", organizations));
    }

    @GetMapping
    public ResponseEntity<ApiResult<List<OrganizationDto>>> getAllOrganizations() {
        log.info("Get all organizations API called");
        List<OrganizationDto> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResult.success("전체 조직 목록 조회 성공", organizations));
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResult<List<OrganizationHierarchyDto>>> getOrganizationHierarchy() {
        log.info("Get organization hierarchy API called");
        List<OrganizationHierarchyDto> hierarchy = organizationService.getOrganizationHierarchy();
        return ResponseEntity.ok(ApiResult.success("조직 계층 구조 조회 성공", hierarchy));
    }

    @PutMapping("/{organizationId}")
    public ResponseEntity<ApiResult<OrganizationDto>> updateOrganization(
            @PathVariable Long organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        log.info("Update organization API called: organizationId={}", organizationId);
        OrganizationDto updatedOrganization = organizationService.updateOrganization(organizationId, request);
        return ResponseEntity.ok(ApiResult.success("조직 정보 수정 성공", updatedOrganization));
    }

    @DeleteMapping("/{organizationId}")
    public ResponseEntity<ApiResult<Void>> deleteOrganization(@PathVariable Long organizationId) {
        log.info("Delete organization API called: organizationId={}", organizationId);
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.ok(ApiResult.success("조직 삭제 성공", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResult<List<OrganizationDto>>> searchOrganizations(@RequestParam String keyword) {
        log.info("Search organizations API called: keyword={}", keyword);
        List<OrganizationDto> organizations = organizationService.searchOrganizations(keyword);
        return ResponseEntity.ok(ApiResult.success("조직 검색 성공", organizations));
    }
}
