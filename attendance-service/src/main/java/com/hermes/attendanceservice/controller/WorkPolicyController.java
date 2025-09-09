package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.attendanceservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.attendanceservice.entity.workpolicy.WorkPolicy;
import com.hermes.attendanceservice.entity.workpolicy.WorkType;
import com.hermes.attendanceservice.repository.workpolicy.WorkPolicyRepository;
import com.hermes.attendanceservice.service.workpolicy.WorkPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workpolicy")
@Tag(name = "Work Policy", description = "근무 정책 관리 API")
public class WorkPolicyController {
    
    private final WorkPolicyRepository workPolicyRepository;
    private final WorkPolicyService workPolicyService;
    
    @Operation(summary = "근무 정책 생성", description = "새로운 근무 정책을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 정책 생성 성공",
            content = @Content(schema = @Schema(implementation = WorkPolicyResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<WorkPolicyResponseDto> createWorkPolicy(
            @Parameter(description = "근무 정책 생성 정보") @Valid @RequestBody WorkPolicyRequestDto requestDto) {
        try {
            log.info("Creating work policy via service: {}", requestDto.getName());
            
            WorkPolicyResponseDto response = workPolicyService.createWorkPolicy(requestDto);
            return ApiResult.success("근무 정책이 성공적으로 생성되었습니다.", response);
            
        } catch (Exception e) {
            log.error("Error creating work policy: {}", requestDto.getName(), e);
            return ApiResult.failure("근무 정책 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Operation(summary = "전체 근무 정책 목록 조회", description = "모든 근무 정책 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 정책 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = WorkPolicyResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ApiResult<List<WorkPolicyResponseDto>> getAllWorkPolicies() {
        try {
            log.info("Get all work policies");
            
            List<WorkPolicy> workPolicies = workPolicyRepository.findAll();
            List<WorkPolicyResponseDto> responses = workPolicies.stream()
                    .map(WorkPolicyResponseDto::from)
                    .toList();
            
            return ApiResult.success("전체 근무 정책 목록을 성공적으로 조회했습니다.", responses);
            
        } catch (Exception e) {
            log.error("Error getting all work policies", e);
            return ApiResult.failure("근무 정책 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Operation(summary = "근무 정책 조회", description = "ID로 특정 근무 정책을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 정책 조회 성공",
            content = @Content(schema = @Schema(implementation = WorkPolicyResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "근무 정책을 찾을 수 없음")
    })
    @GetMapping("/{workPolicyId}")
    public ApiResult<WorkPolicyResponseDto> getWorkPolicyById(
            @Parameter(description = "근무 정책 ID") @PathVariable Long workPolicyId) {
        try {
            log.info("Get work policy by id: {}", workPolicyId);
            
            WorkPolicy workPolicy = workPolicyRepository.findById(workPolicyId)
                    .orElse(null);
            
            if (workPolicy == null) {
                return ApiResult.failure("근무 정책을 찾을 수 없습니다: " + workPolicyId);
            }
            
            WorkPolicyResponseDto response = WorkPolicyResponseDto.from(workPolicy);
            return ApiResult.success("근무 정책 정보를 성공적으로 조회했습니다.", response);
            
        } catch (Exception e) {
            log.error("Error getting work policy by id: {}", workPolicyId, e);
            return ApiResult.failure("근무 정책 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "근무 정책 수정", description = "기존 근무 정책을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 정책 수정 성공",
            content = @Content(schema = @Schema(implementation = WorkPolicyResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "근무 정책을 찾을 수 없음")
    })
    @PutMapping("/{workPolicyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<WorkPolicyResponseDto> updateWorkPolicy(
            @Parameter(description = "근무 정책 ID") @PathVariable Long workPolicyId,
            @Parameter(description = "근무 정책 수정 정보") @Valid @RequestBody com.hermes.attendanceservice.dto.workpolicy.WorkPolicyUpdateDto updateDto) {
        try {
            log.info("Updating work policy via service: {}", workPolicyId);
            WorkPolicyResponseDto response = workPolicyService.updateWorkPolicy(workPolicyId, updateDto);
            return ApiResult.success("근무 정책이 성공적으로 수정되었습니다.", response);
        } catch (Exception e) {
            log.error("Error updating work policy: {}", workPolicyId, e);
            return ApiResult.failure("근무 정책 수정에 실패했습니다: " + e.getMessage());
        }
    }
} 
