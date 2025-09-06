package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.leave.EmployeeLeaveBalanceResponseDto;
import com.hermes.attendanceservice.dto.leave.EmployeeLeaveBalanceSummaryDto;
import com.hermes.attendanceservice.entity.leave.LeaveType;
import com.hermes.attendanceservice.service.leave.EmployeeLeaveBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "직원 연차 잔액 관리", description = "직원별 연차 잔액 조회, 부여, 사용, 복구 등을 관리하는 API")
@RestController
@RequestMapping("/api/leave-balance")
@RequiredArgsConstructor
@Slf4j
public class EmployeeLeaveBalanceController {
    
    private final EmployeeLeaveBalanceService employeeLeaveBalanceService;
    
    @Operation(summary = "연차 자동 부여", description = "직원의 근무년수에 따라 연차를 자동으로 부여합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "연차 부여 성공", 
                    content = @Content(schema = @Schema(implementation = ApiResult.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (직원 정보 없음, 근무정책 없음 등)")
    })
    @PostMapping("/grant-annual/{employeeId}")
    public ResponseEntity<ApiResult<List<EmployeeLeaveBalanceResponseDto>>> grantAnnualLeave(
            @Parameter(description = "직원 ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "기준일 (기본값: 오늘)", example = "2024-01-01") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate) {
        
        LocalDate grantDate = baseDate != null ? baseDate : LocalDate.now();
        log.info("연차 자동 부여 요청: employeeId={}, baseDate={}", employeeId, grantDate);
        
        List<EmployeeLeaveBalanceResponseDto> responses = employeeLeaveBalanceService.grantAnnualLeave(employeeId, grantDate);
        return ResponseEntity.ok(ApiResult.success(responses));
    }
    
    @Operation(summary = "특정 타입 잔여 연차 조회", description = "직원의 특정 연차 타입별 잔여 일수를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "직원 또는 연차 정보를 찾을 수 없음")
    })
    @GetMapping("/remaining/{employeeId}")
    public ResponseEntity<ApiResult<Integer>> getRemainingLeave(
            @Parameter(description = "직원 ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "연차 타입", required = true, schema = @Schema(implementation = LeaveType.class)) 
            @RequestParam LeaveType leaveType) {
        
        Integer remainingDays = employeeLeaveBalanceService.getRemainingLeave(employeeId, leaveType);
        return ResponseEntity.ok(ApiResult.success(remainingDays));
    }
    
    @Operation(summary = "전체 잔여 연차 조회", description = "직원의 모든 연차 타입을 합한 총 잔여 일수를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "직원 정보를 찾을 수 없음")
    })
    @GetMapping("/remaining-total/{employeeId}")
    public ResponseEntity<ApiResult<Integer>> getTotalRemainingLeave(
            @Parameter(description = "직원 ID", required = true) @PathVariable Long employeeId) {
        Integer totalRemainingDays = employeeLeaveBalanceService.getTotalRemainingLeave(employeeId);
        return ResponseEntity.ok(ApiResult.success(totalRemainingDays));
    }
    
    @Operation(summary = "연차 잔액 상세 조회", description = "직원의 연차 타입별 상세 잔액 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "직원 정보를 찾을 수 없음")
    })
    @GetMapping("/details/{employeeId}")
    public ResponseEntity<ApiResult<List<EmployeeLeaveBalanceResponseDto>>> getLeaveBalances(
            @Parameter(description = "직원 ID", required = true) @PathVariable Long employeeId) {
        
        List<EmployeeLeaveBalanceResponseDto> balances = employeeLeaveBalanceService.getLeaveBalances(employeeId);
        return ResponseEntity.ok(ApiResult.success(balances));
    }
    
    @Operation(summary = "연차 잔액 요약 조회", description = "직원의 연차 사용 현황을 요약하여 조회합니다. (총 부여, 사용, 잔여, 사용률 등)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "직원 정보를 찾을 수 없음")
    })
    @GetMapping("/summary/{employeeId}")
    public ResponseEntity<ApiResult<EmployeeLeaveBalanceSummaryDto>> getLeaveBalanceSummary(
            @Parameter(description = "직원 ID", required = true) @PathVariable Long employeeId) {
        
        EmployeeLeaveBalanceSummaryDto summary = employeeLeaveBalanceService.getLeaveBalanceSummary(employeeId);
        return ResponseEntity.ok(ApiResult.success(summary));
    }
    
    @Operation(summary = "연차 초기화 및 재부여", description = "특정 직원의 기존 연차를 삭제하고 새로운 연차를 부여합니다. (년차 변경 시 사용)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "초기화 및 재부여 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (직원 정보 없음, 근무정책 없음 등)")
    })
    @PostMapping("/reset/{employeeId}")
    public ResponseEntity<ApiResult<List<EmployeeLeaveBalanceResponseDto>>> resetAndGrantAnnualLeave(
            @Parameter(description = "직원 ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "새로운 연차 부여일 (기본값: 오늘)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newGrantDate) {
        
        LocalDate grantDate = newGrantDate != null ? newGrantDate : LocalDate.now();
        log.info("직원 연차 초기화 및 재부여 요청: employeeId={}, newGrantDate={}", employeeId, grantDate);
        
        List<EmployeeLeaveBalanceResponseDto> responses = employeeLeaveBalanceService.resetAndGrantAnnualLeave(employeeId, grantDate);
        return ResponseEntity.ok(ApiResult.success(responses));
    }
    
    @Operation(summary = "연차 복구", description = "연차 신청 취소 시 사용한 연차를 다시 복구합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "연차 복구 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (복구할 연차보다 사용한 연차가 적음)")
    })
    @PostMapping("/restore/{employeeId}")
    public ResponseEntity<ApiResult<String>> restoreLeave(
            @Parameter(description = "직원 ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "연차 타입", required = true, schema = @Schema(implementation = LeaveType.class)) 
            @RequestParam LeaveType leaveType,
            @Parameter(description = "복구할 연차 일수", required = true, example = "3") 
            @RequestParam Integer days) {
        
        log.info("연차 복구 요청: employeeId={}, leaveType={}, days={}", employeeId, leaveType, days);
        
        employeeLeaveBalanceService.restoreLeave(employeeId, leaveType, days);
        return ResponseEntity.ok(ApiResult.success("연차 복구가 완료되었습니다."));
    }
    
    @Operation(summary = "전체 직원 연차 초기화", description = "모든 직원의 연차를 초기화하고 재부여합니다. (관리자 전용 - 신년도 시작 시 사용)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "전체 초기화 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/reset-all")
    public ResponseEntity<ApiResult<String>> resetAllEmployeesAnnualLeave(
            @Parameter(description = "새로운 연차 부여일", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newGrantDate) {
        
        log.info("모든 직원 연차 초기화 및 재부여 요청: newGrantDate={}", newGrantDate);
        
        employeeLeaveBalanceService.resetAllEmployeesAnnualLeave(newGrantDate);
        return ResponseEntity.ok(ApiResult.success("모든 직원의 연차 초기화 및 재부여가 완료되었습니다."));
    }
} 