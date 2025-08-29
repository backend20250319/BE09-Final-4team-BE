package com.hermes.attendanceservice.controller;

import com.hermes.attendanceservice.dto.workmonitor.WorkMonitorDto;
import com.hermes.attendanceservice.service.workmonitor.WorkMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/work-monitor")
@RequiredArgsConstructor
@Slf4j
public class WorkMonitorController {
    
    private final WorkMonitorService workMonitorService;
    
    /**
     * 현재 사용자의 권한이 ADMIN인지 확인
     */
    private boolean isAdmin(String authorization) {
        try {
            // JWT 토큰에서 권한 정보를 추출하는 로직
            // 실제 구현에서는 JWT 토큰을 파싱하여 권한을 확인해야 함
            // 여기서는 간단히 헤더에 ADMIN이 포함되어 있는지 확인
            return authorization != null && authorization.contains("ADMIN");
        } catch (Exception e) {
            log.error("권한 확인 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * 오늘 날짜의 근무 모니터링 데이터 조회
     */
    @GetMapping("/today")
    public ResponseEntity<WorkMonitorDto> getTodayWorkMonitor(
            @RequestHeader("Authorization") String authorization) {
        log.info("Fetching today's work monitor data");
        
        // ADMIN 권한 체크
        if (!isAdmin(authorization)) {
            log.warn("권한 부족: 오늘 근무 모니터링 조회 요청이 거부됨");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        WorkMonitorDto workMonitorDto = workMonitorService.getTodayWorkMonitor(authorization);
        return ResponseEntity.ok(workMonitorDto);
    }
    
    /**
     * 특정 날짜의 근무 모니터링 데이터 조회
     */
    @GetMapping("/{date}")
    public ResponseEntity<WorkMonitorDto> getWorkMonitorByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestHeader("Authorization") String authorization) {
        log.info("Fetching work monitor data for date: {}", date);
        
        // ADMIN 권한 체크
        if (!isAdmin(authorization)) {
            log.warn("권한 부족: 특정 날짜 근무 모니터링 조회 요청이 거부됨");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        WorkMonitorDto workMonitorDto = workMonitorService.getWorkMonitorByDate(date, authorization);
        return ResponseEntity.ok(workMonitorDto);
    }
    
    /**
     * 출석 버튼 클릭 시 근무 모니터링 데이터 갱신
     */
    @PostMapping("/update/{date}")
    public ResponseEntity<WorkMonitorDto> updateWorkMonitorData(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestHeader("Authorization") String authorization) {
        log.info("Updating work monitor data for date: {}", date);
        
        // ADMIN 권한 체크
        if (!isAdmin(authorization)) {
            log.warn("권한 부족: 근무 모니터링 데이터 갱신 요청이 거부됨");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(date, authorization);
        return ResponseEntity.ok(workMonitorDto);
    }
    
    /**
     * 오늘 날짜의 근무 모니터링 데이터 갱신
     */
    @PostMapping("/update/today")
    public ResponseEntity<WorkMonitorDto> updateTodayWorkMonitorData(
            @RequestHeader("Authorization") String authorization) {
        log.info("Updating today's work monitor data");
        
        // ADMIN 권한 체크
        if (!isAdmin(authorization)) {
            log.warn("권한 부족: 오늘 근무 모니터링 데이터 갱신 요청이 거부됨");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        WorkMonitorDto workMonitorDto = workMonitorService.updateWorkMonitorData(LocalDate.now(), authorization);
        return ResponseEntity.ok(workMonitorDto);
    }
} 