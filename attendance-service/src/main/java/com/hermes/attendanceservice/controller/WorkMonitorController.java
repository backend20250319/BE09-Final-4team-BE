package com.hermes.attendanceservice.controller;

import com.hermes.auth.principal.UserPrincipal;
import com.hermes.attendanceservice.dto.workmonitor.WorkMonitorDto;
import com.hermes.attendanceservice.service.workmonitor.WorkMonitorService;
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
    
    /**
     * 오늘 날짜의 근무 모니터링 데이터 조회
     */
    @GetMapping("/today")
    public ResponseEntity<WorkMonitorDto> getTodayWorkMonitor() {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Fetching today's work monitor data for user: {}", user.getUserId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.getTodayWorkMonitor();
        return ResponseEntity.ok(workMonitorDto);
    }
    
    /**
     * 특정 날짜의 근무 모니터링 데이터 조회
     */
    @GetMapping("/{date}")
    public ResponseEntity<WorkMonitorDto> getWorkMonitorByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Fetching work monitor data for date: {} by user: {}", date, user.getUserId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.getWorkMonitorByDate(date);
        return ResponseEntity.ok(workMonitorDto);
    }
    
    /**
     * 출석 버튼 클릭 시 근무 모니터링 데이터 갱신
     */
    @PostMapping("/update/{date}")
    public ResponseEntity<WorkMonitorDto> updateWorkMonitorData(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Updating work monitor data for date: {} by user: {}", date, user.getUserId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(date);
        return ResponseEntity.ok(workMonitorDto);
    }
    
    /**
     * 오늘 날짜의 근무 모니터링 데이터 갱신
     */
    @PostMapping("/update/today")
    public ResponseEntity<WorkMonitorDto> updateTodayWorkMonitorData() {
        UserPrincipal user = getUserPrincipal();
        if (user == null) {
            log.error("User authentication failed - UserPrincipal is null");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Updating today's work monitor data by user: {}", user.getUserId());
        
        WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(LocalDate.now());
        return ResponseEntity.ok(workMonitorDto);
    }
} 