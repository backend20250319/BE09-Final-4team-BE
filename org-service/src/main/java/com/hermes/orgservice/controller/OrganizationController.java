package com.hermes.orgservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.orgservice.dto.CreateOrganizationRequest;
import com.hermes.orgservice.dto.OrganizationDto;
import com.hermes.orgservice.dto.OrganizationHierarchyDto;
import com.hermes.orgservice.dto.UpdateOrganizationRequest;
import com.hermes.orgservice.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "조직 관리 API", description = "조직 구조 생성, 조회, 수정, 삭제 및 계층 구조 관리 기능 제공")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "조직 생성", description = "새로운 조직을 생성합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "조직 생성 성공", 
                     content = @Content(schema = @Schema(implementation = OrganizationDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
        @ApiResponse(responseCode = "409", description = "중복된 조직명")
    })
    public ResponseEntity<ApiResult<OrganizationDto>> createOrganization(
            @Parameter(description = "생성할 조직 정보", required = true) 
            @Valid @RequestBody CreateOrganizationRequest request) {
        log.info("Create organization API called: {}", request.getName());
        OrganizationDto createdOrganization = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("조직 생성 성공", createdOrganization));
    }

    @GetMapping("/{organizationId}")
    @Operation(summary = "조직 정보 조회", description = "특정 조직의 상세 정보를 조회합니다. 인증된 사용자만 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조직 정보 조회 성공", 
                     content = @Content(schema = @Schema(implementation = OrganizationDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "조직을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<OrganizationDto>> getOrganization(
            @Parameter(description = "조회할 조직 ID", required = true, example = "1") 
            @PathVariable Long organizationId) {
        log.info("Get organization API called: organizationId={}", organizationId);
        OrganizationDto organization = organizationService.getOrganization(organizationId);
        return ResponseEntity.ok(ApiResult.success("조직 정보 조회 성공", organization));
    }

    @GetMapping("/root")
    @Operation(summary = "최상위 조직 목록 조회", description = "상위 조직이 없는 최상위 조직들의 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "최상위 조직 목록 조회 성공", 
                     content = @Content(schema = @Schema(implementation = OrganizationDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResult<List<OrganizationDto>>> getRootOrganizations() {
        log.info("Get root organizations API called");
        List<OrganizationDto> organizations = organizationService.getRootOrganizations();
        return ResponseEntity.ok(ApiResult.success("최상위 조직 목록 조회 성공", organizations));
    }

    @GetMapping
    @Operation(summary = "전체 조직 목록 조회", description = "시스템에 등록된 모든 조직의 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "전체 조직 목록 조회 성공", 
                     content = @Content(schema = @Schema(implementation = OrganizationDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResult<List<OrganizationDto>>> getAllOrganizations() {
        log.info("Get all organizations API called");
        List<OrganizationDto> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResult.success("전체 조직 목록 조회 성공", organizations));
    }

    @GetMapping("/hierarchy")
    @Operation(summary = "조직 계층 구조 조회", description = "조직의 전체 계층 구조를 트리 형태로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조직 계층 구조 조회 성공", 
                     content = @Content(schema = @Schema(implementation = OrganizationHierarchyDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResult<List<OrganizationHierarchyDto>>> getOrganizationHierarchy() {
        log.info("Get organization hierarchy API called");
        List<OrganizationHierarchyDto> hierarchy = organizationService.getOrganizationHierarchy();
        return ResponseEntity.ok(ApiResult.success("조직 계층 구조 조회 성공", hierarchy));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{organizationId}")
    @Operation(summary = "조직 정보 수정", description = "기존 조직의 정보를 수정합니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조직 정보 수정 성공", 
                     content = @Content(schema = @Schema(implementation = OrganizationDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
        @ApiResponse(responseCode = "404", description = "조직을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "중복된 조직명")
    })
    public ResponseEntity<ApiResult<OrganizationDto>> updateOrganization(
            @Parameter(description = "수정할 조직 ID", required = true, example = "1") 
            @PathVariable Long organizationId,
            @Parameter(description = "수정할 조직 정보", required = true) 
            @Valid @RequestBody UpdateOrganizationRequest request) {
        log.info("Update organization API called: organizationId={}", organizationId);
        OrganizationDto updatedOrganization = organizationService.updateOrganization(organizationId, request);
        return ResponseEntity.ok(ApiResult.success("조직 정보 수정 성공", updatedOrganization));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{organizationId}")
    @Operation(summary = "조직 삭제", description = "조직을 삭제합니다. 하위 조직이나 배정된 직원이 있으면 삭제할 수 없습니다. 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조직 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
        @ApiResponse(responseCode = "404", description = "조직을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "삭제 불가능한 조직 (하위 조직 또는 배정된 직원 존재)")
    })
    public ResponseEntity<ApiResult<Void>> deleteOrganization(
            @Parameter(description = "삭제할 조직 ID", required = true, example = "1") 
            @PathVariable Long organizationId) {
        log.info("Delete organization API called: organizationId={}", organizationId);
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.ok(ApiResult.success("조직 삭제 성공", null));
    }

    @GetMapping("/search")
    @Operation(summary = "조직 검색", description = "키워드를 사용하여 조직을 검색합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조직 검색 성공", 
                     content = @Content(schema = @Schema(implementation = OrganizationDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResult<List<OrganizationDto>>> searchOrganizations(
            @Parameter(description = "검색 키워드", required = true, example = "개발팀") 
            @RequestParam String keyword) {
        log.info("Search organizations API called: keyword={}", keyword);
        List<OrganizationDto> organizations = organizationService.searchOrganizations(keyword);
        return ResponseEntity.ok(ApiResult.success("조직 검색 성공", organizations));
    }
}
