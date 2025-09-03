package com.hermes.attendanceservice.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/work-monitor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Work Monitor", description = "근무 모니터링 API")
public class WorkMonitorController {
    
    private final WorkMonitorService workMonitorService;
    
    /**
     * SecurityContext에서 UserPrincipal을 추출하는 헬퍼 메서드
     */
    private UserPrincipal getUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getDetails();
        }
        return null;
    }
    
    @Operation(summary = "오늘 근무 모니터링 조회", description = "오늘 날짜의 근무 모니터링 데이터를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 조회 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/today")
    public ResponseEntity<WorkMonitorDto> getTodayWorkMonitor() {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Fetching today's work monitor data for user: {}", user.getId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.getTodayWorkMonitor();
        return ResponseEntity.ok(workMonitorDto);
    }
    
    @Operation(summary = "특정 날짜 근무 모니터링 조회", description = "특정 날짜의 근무 모니터링 데이터를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 조회 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{date}")
    public ResponseEntity<WorkMonitorDto> getWorkMonitorByDate(
            @Parameter(description = "날짜 (YYYY-MM-DD)") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Fetching work monitor data for date: {} by user: {}", date, user.getId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.getWorkMonitorByDate(date);
        return ResponseEntity.ok(workMonitorDto);
    }
    
    @Operation(summary = "근무 모니터링 데이터 갱신", description = "특정 날짜의 근무 모니터링 데이터를 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 갱신 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/update/{date}")
    public ResponseEntity<WorkMonitorDto> updateWorkMonitorData(
            @Parameter(description = "날짜 (YYYY-MM-DD)") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Updating work monitor data for date: {} by user: {}", date, user.getId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(date);
        return ResponseEntity.ok(workMonitorDto);
    }
    
    @Operation(summary = "오늘 근무 모니터링 데이터 갱신", description = "오늘 날짜의 근무 모니터링 데이터를 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 모니터링 갱신 성공",
            content = @Content(schema = @Schema(implementation = WorkMonitorDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/update/today")
    public ResponseEntity<WorkMonitorDto> updateTodayWorkMonitorData() {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Updating today's work monitor data by user: {}", user.getId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(LocalDate.now());
        return ResponseEntity.ok(workMonitorDto);
    }
} 