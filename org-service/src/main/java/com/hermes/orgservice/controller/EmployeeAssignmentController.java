package com.hermes.orgservice.controller;

import com.hermes.api.common.ApiResult;
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
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;

@Slf4j
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "직원 배정 관리 API", description = "직원을 조직에 배정하고 관리하는 기능 제공")
public class EmployeeAssignmentController {

    private final EmployeeAssignmentService employeeAssignmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "직원 배정 생성", description = "조직에 직원을 배정합니다.")
    @ApiResponse(responseCode = "201", description = "직원 배정 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    @ApiResponse(responseCode = "403", description = "권한이 없는 요청")
    @ApiResponse(responseCode = "404", description = "조직 또는 직원을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<EmployeeAssignmentDto>> createAssignment(
            @Parameter(description = "생성할 직원 배정 정보", required = true) 
            @Valid @RequestBody CreateAssignmentRequest request) {
        log.info("Create employee assignment API called: employeeId={}, organizationId={}", 
                request.getEmployeeId(), request.getOrganizationId());
        EmployeeAssignmentDto createdAssignment = employeeAssignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("직원 배정 생성 성공", createdAssignment));
    }

    @GetMapping("/{assignmentId}")
    @Operation(summary = "직원 배정 단건 조회", description = "배정 ID로 직원 배정 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배정 정보 조회 성공")
    @ApiResponse(responseCode = "404", description = "배정을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<EmployeeAssignmentDto>> getAssignment(
            @Parameter(description = "조회할 배정 ID", required = true, example = "1") 
            @PathVariable Long assignmentId) {
        log.info("Get employee assignment API called: assignmentId={}", assignmentId);
        EmployeeAssignmentDto assignment = employeeAssignmentService.getAssignment(assignmentId);
        return ResponseEntity.ok(ApiResult.success("배정 정보 조회 성공", assignment));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "직원별 배정 목록 조회", description = "직원 ID로 해당 직원의 모든 배정 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직원 배정 목록 조회 성공")
    @ApiResponse(responseCode = "404", description = "직원을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<List<EmployeeAssignmentDto>>> getAssignmentsByEmployeeId(
            @Parameter(description = "조회할 직원 ID", required = true, example = "1") 
            @PathVariable Long employeeId) {
        log.info("Get employee assignments API called: employeeId={}", employeeId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResult.success("직원 배정 목록 조회 성공", assignments));
    }

    @GetMapping
    @Operation(summary = "전체 배정 목록 조회", description = "모든 배정 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "전체 배정 목록 조회 성공")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<List<EmployeeAssignmentDto>>> getAllAssignments() {
        log.info("Get all assignments API called");
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getAllAssignments();
        return ResponseEntity.ok(ApiResult.success("전체 배정 목록 조회 성공", assignments));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "조직별 배정 목록 조회", description = "조직 ID로 해당 조직의 모든 배정 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조직 배정 목록 조회 성공")
    @ApiResponse(responseCode = "404", description = "조직을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<List<EmployeeAssignmentDto>>> getAssignmentsByOrganizationId(
            @Parameter(description = "조회할 조직 ID", required = true, example = "1") 
            @PathVariable Long organizationId) {
        log.info("Get organization assignments API called: organizationId={}", organizationId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getAssignmentsByOrganizationId(organizationId);
        return ResponseEntity.ok(ApiResult.success("조직 배정 목록 조회 성공", assignments));
    }

    @GetMapping("/employee/{employeeId}/primary")
    @Operation(summary = "직원 메인 부서 조회", description = "직원 ID로 해당 직원의 메인 부서 배정 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직원 메인 부서 조회 성공")
    @ApiResponse(responseCode = "404", description = "직원을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<List<EmployeeAssignmentDto>>> getPrimaryAssignmentsByEmployeeId(
            @Parameter(description = "조회할 직원 ID", required = true, example = "1") 
            @PathVariable Long employeeId) {
        log.info("Get primary assignments API called: employeeId={}", employeeId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getPrimaryAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResult.success("직원 메인 부서 조회 성공", assignments));
    }

    @GetMapping("/organization/{organizationId}/leaders")
    @Operation(summary = "조직 리더 목록 조회", description = "조직 ID로 해당 조직의 리더 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조직 리더 목록 조회 성공")
    @ApiResponse(responseCode = "404", description = "조직을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<List<EmployeeAssignmentDto>>> getLeadersByOrganizationId(
            @Parameter(description = "조회할 조직 ID", required = true, example = "1") 
            @PathVariable Long organizationId) {
        log.info("Get organization leaders API called: organizationId={}", organizationId);
        List<EmployeeAssignmentDto> assignments = employeeAssignmentService.getLeadersByOrganizationId(organizationId);
        return ResponseEntity.ok(ApiResult.success("조직 리더 목록 조회 성공", assignments));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{assignmentId}")
    @Operation(summary = "직원 배정 정보 수정", description = "배정 ID로 직원 배정 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "배정 정보 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    @ApiResponse(responseCode = "403", description = "권한이 없는 요청")
    @ApiResponse(responseCode = "404", description = "배정을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<EmployeeAssignmentDto>> updateAssignment(
            @Parameter(description = "수정할 배정 ID", required = true, example = "1") 
            @PathVariable Long assignmentId,
            @Parameter(description = "수정할 배정 정보", required = true) 
            @Valid @RequestBody CreateAssignmentRequest request) {
        log.info("Update employee assignment API called: assignmentId={}", assignmentId);
        EmployeeAssignmentDto updatedAssignment = employeeAssignmentService.updateAssignment(assignmentId, request);
        return ResponseEntity.ok(ApiResult.success("배정 정보 수정 성공", updatedAssignment));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{assignmentId}")
    @Operation(summary = "직원 배정 삭제", description = "배정 ID로 직원 배정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "배정 정보 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    @ApiResponse(responseCode = "403", description = "권한이 없는 요청")
    @ApiResponse(responseCode = "404", description = "배정을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ApiResult<Void>> deleteAssignment(
            @Parameter(description = "삭제할 배정 ID", required = true, example = "1") 
            @PathVariable Long assignmentId) {
        log.info("Delete employee assignment API called: assignmentId={}", assignmentId);
        employeeAssignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.ok(ApiResult.success("배정 정보 삭제 성공", null));
    }
}
