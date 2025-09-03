package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveRequestDto;
import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveResponseDto;
import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveUpdateDto;
import com.hermes.attendanceservice.service.workpolicy.AnnualLeaveService;
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
@RequestMapping("/api/annual-leaves")
@Tag(name = "Annual Leave", description = "연차 정책 관리 API")
public class AnnualLeaveController {

    private final AnnualLeaveService annualLeaveService;

    @Operation(summary = "연차 정책 생성", description = "새로운 연차 정책을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "연차 정책 생성 성공",
            content = @Content(schema = @Schema(implementation = AnnualLeaveResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/work-policies/{workPolicyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<AnnualLeaveResponseDto> createAnnualLeave(
            @Parameter(description = "근무 정책 ID") @PathVariable Long workPolicyId,
            @Parameter(description = "연차 정책 생성 정보") @Valid @RequestBody AnnualLeaveRequestDto requestDto) {
        try {
            log.info("연차 정책 생성 요청: workPolicyId={}, name={}", workPolicyId, requestDto.getName());
            
            AnnualLeaveResponseDto response = annualLeaveService.createAnnualLeave(workPolicyId, requestDto);
            
            return ApiResult.success("연차 정책이 성공적으로 생성되었습니다.", response);
        } catch (Exception e) {
            log.error("연차 정책 생성 실패: {}", e.getMessage());
            return ApiResult.failure("연차 정책 생성에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "연차 정책 조회", description = "ID로 특정 연차 정책을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "연차 정책 조회 성공",
            content = @Content(schema = @Schema(implementation = AnnualLeaveResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "연차 정책을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ApiResult<AnnualLeaveResponseDto> getAnnualLeaveById(
            @Parameter(description = "연차 정책 ID") @PathVariable Long id) {
        try {
            log.info("연차 정책 조회 요청: ID={}", id);
            
            AnnualLeaveResponseDto response = annualLeaveService.getAnnualLeaveById(id);
            
            return ApiResult.success("연차 정책을 성공적으로 조회했습니다.", response);
        } catch (Exception e) {
            log.error("연차 정책 조회 실패: {}", e.getMessage());
            return ApiResult.failure("연차 정책 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "근무 정책별 연차 정책 목록 조회", description = "특정 근무 정책에 속한 모든 연차 정책을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "연차 정책 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = AnnualLeaveResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "근무 정책을 찾을 수 없음")
    })
    @GetMapping("/work-policies/{workPolicyId}")
    public ApiResult<List<AnnualLeaveResponseDto>> getAnnualLeavesByWorkPolicyId(
            @Parameter(description = "근무 정책 ID") @PathVariable Long workPolicyId) {
        try {
            log.info("근무 정책 연차 목록 조회 요청: workPolicyId={}", workPolicyId);
            
            List<AnnualLeaveResponseDto> response = annualLeaveService.getAnnualLeavesByWorkPolicyId(workPolicyId);
            
            return ApiResult.success("연차 정책 목록을 성공적으로 조회했습니다.", response);
        } catch (Exception e) {
            log.error("연차 정책 목록 조회 실패: {}", e.getMessage());
            return ApiResult.failure("연차 정책 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "연차 정책 수정", description = "기존 연차 정책을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "연차 정책 수정 성공",
            content = @Content(schema = @Schema(implementation = AnnualLeaveResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "연차 정책을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<AnnualLeaveResponseDto> updateAnnualLeave(
            @Parameter(description = "연차 정책 ID") @PathVariable Long id,
            @Parameter(description = "연차 정책 수정 정보") @Valid @RequestBody AnnualLeaveUpdateDto updateDto) {
        try {
            log.info("연차 정책 수정 요청: ID={}", id);
            
            AnnualLeaveResponseDto response = annualLeaveService.updateAnnualLeave(id, updateDto);
            
            return ApiResult.success("연차 정책이 성공적으로 수정되었습니다.", response);
        } catch (Exception e) {
            log.error("연차 정책 수정 실패: {}", e.getMessage());
            return ApiResult.failure("연차 정책 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "연차 정책 삭제", description = "연차 정책을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "연차 정책 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "연차 정책을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> deleteAnnualLeave(
            @Parameter(description = "연차 정책 ID") @PathVariable Long id) {
        try {
            log.info("연차 정책 삭제 요청: ID={}", id);
            
            annualLeaveService.deleteAnnualLeave(id);
            
            return ApiResult.success("연차 정책이 성공적으로 삭제되었습니다.", null);
        } catch (Exception e) {
            log.error("연차 정책 삭제 실패: {}", e.getMessage());
            return ApiResult.failure("연차 정책 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "근무 정책별 총 연차 일수 계산", description = "특정 근무 정책의 총 연차 일수를 계산합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "총 연차 일수 계산 성공"),
        @ApiResponse(responseCode = "404", description = "근무 정책을 찾을 수 없음")
    })
    @GetMapping("/work-policies/{workPolicyId}/total-leave-days")
    public ApiResult<Integer> calculateTotalLeaveDays(
            @Parameter(description = "근무 정책 ID") @PathVariable Long workPolicyId) {
        try {
            log.info("총 연차 일수 계산 요청: workPolicyId={}", workPolicyId);
            
            Integer totalDays = annualLeaveService.calculateTotalLeaveDays(workPolicyId);
            
            return ApiResult.success("총 연차 일수를 성공적으로 계산했습니다.", totalDays);
        } catch (Exception e) {
            log.error("총 연차 일수 계산 실패: {}", e.getMessage());
            return ApiResult.failure("총 연차 일수 계산에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "근무 정책별 총 휴일 일수 계산", description = "특정 근무 정책의 총 휴일 일수를 계산합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "총 휴일 일수 계산 성공"),
        @ApiResponse(responseCode = "404", description = "근무 정책을 찾을 수 없음")
    })
    @GetMapping("/work-policies/{workPolicyId}/total-holiday-days")
    public ApiResult<Integer> calculateTotalHolidayDays(
            @Parameter(description = "근무 정책 ID") @PathVariable Long workPolicyId) {
        try {
            log.info("총 휴일 일수 계산 요청: workPolicyId={}", workPolicyId);
            
            Integer totalDays = annualLeaveService.calculateTotalHolidayDays(workPolicyId);
            
            return ApiResult.success("총 휴일 일수를 성공적으로 계산했습니다.", totalDays);
        } catch (Exception e) {
            log.error("총 휴일 일수 계산 실패: {}", e.getMessage());
            return ApiResult.failure("총 휴일 일수 계산에 실패했습니다: " + e.getMessage());
        }
    }
} 