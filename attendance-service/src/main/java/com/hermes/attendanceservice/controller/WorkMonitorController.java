package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.attendanceservice.dto.workmonitor.WorkMonitorDto;
import com.hermes.attendanceservice.service.workmonitor.WorkMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/work-monitor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Work Monitor", description = "근무 모니터링 API")
public class WorkMonitorController {
    
    private final WorkMonitorService workMonitorService;
    
    @Operation(summary = "오늘 근무 모니터링 조회", description = "오늘 날짜의 근무 모니터링 데이터를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 조회 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/today")
    public ApiResult<WorkMonitorDto> getTodayWorkMonitor(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            if (user == null) {
                log.error("User authentication failed - UserPrincipal is null");
                return ApiResult.failure("인증이 필요합니다.");
            }
            
            log.info("Fetching today's work monitor data for user: {}", user.getId());
            
            WorkMonitorDto workMonitorDto = workMonitorService.getTodayWorkMonitor();
            return ApiResult.success("오늘 근무 모니터링 조회 성공", workMonitorDto);
        } catch (Exception e) {
            log.error("Failed to get today's work monitor data", e);
            return ApiResult.failure("근무 모니터링 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Operation(summary = "특정 날짜 근무 모니터링 조회", description = "특정 날짜의 근무 모니터링 데이터를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 조회 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{date}")
    public ApiResult<WorkMonitorDto> getWorkMonitorByDate(
            @Parameter(description = "날짜 (YYYY-MM-DD)") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            if (user == null) {
                log.error("User authentication failed - UserPrincipal is null");
                return ApiResult.failure("인증이 필요합니다.");
            }
            
            log.info("Fetching work monitor data for date: {} by user: {}", date, user.getId());
            
            WorkMonitorDto workMonitorDto = workMonitorService.getWorkMonitorByDate(date);
            return ApiResult.success("근무 모니터링 조회 성공", workMonitorDto);
        } catch (Exception e) {
            log.error("Failed to get work monitor data for date: {}", date, e);
            return ApiResult.failure("근무 모니터링 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Operation(summary = "근무 모니터링 데이터 갱신", description = "특정 날짜의 근무 모니터링 데이터를 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 갱신 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/update/{date}")
    public ApiResult<WorkMonitorDto> updateWorkMonitorData(
            @Parameter(description = "날짜 (YYYY-MM-DD)") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            if (user == null) {
                log.error("User authentication failed - UserPrincipal is null");
                return ApiResult.failure("인증이 필요합니다.");
            }
            
            log.info("Updating work monitor data for date: {} by user: {}", date, user.getId());
            
            WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(date);
            return ApiResult.success("근무 모니터링 갱신 성공", workMonitorDto);
        } catch (Exception e) {
            log.error("Failed to update work monitor data for date: {}", date, e);
            return ApiResult.failure("근무 모니터링 갱신에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Operation(summary = "오늘 근무 모니터링 데이터 갱신", description = "오늘 날짜의 근무 모니터링 데이터를 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 갱신 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/update/today")
    public ApiResult<WorkMonitorDto> updateTodayWorkMonitorData(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            if (user == null) {
                log.error("User authentication failed - UserPrincipal is null");
                return ApiResult.failure("인증이 필요합니다.");
            }
            
            log.info("Updating today's work monitor data by user: {}", user.getId());
            
            WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(LocalDate.now());
            return ApiResult.success("오늘 근무 모니터링 갱신 성공", workMonitorDto);
        } catch (Exception e) {
            log.error("Failed to update today's work monitor data", e);
            return ApiResult.failure("근무 모니터링 갱신에 실패했습니다: " + e.getMessage());
        }
    }
} 