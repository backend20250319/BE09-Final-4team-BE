package com.hermes.workpolicyservice.controller;

import com.hermes.workpolicyservice.dto.WorkPolicyRequestDto;
import com.hermes.workpolicyservice.service.WorkPolicyCalculationService;
import com.hermes.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/work-policies/calculations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WorkPolicyCalculationController {
    
    private final WorkPolicyCalculationService calculationService;
    
    /**
     * 총 근무 시간 계산 (분 단위)
     */
    @PostMapping("/total-work-minutes")
    public ResponseEntity<ApiResponse<Integer>> calculateTotalWorkMinutes(
            @RequestParam Integer workHours,
            @RequestParam Integer workMinutes) {
        log.info("총 근무 시간 계산 요청: {}시간 {}분", workHours, workMinutes);
        
        try {
            int totalMinutes = calculationService.calculateTotalWorkMinutes(workHours, workMinutes);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("총 근무 시간 계산 완료", totalMinutes));
                            
        } catch (Exception e) {
            log.error("총 근무 시간 계산 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 노동법 기준 근무 시간 조회
     */
    @GetMapping("/labor-law-standards")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getLaborLawStandards() {
        log.info("노동법 기준 근무 시간 조회 요청");
        
        try {
            int weeklyMinutes = calculationService.calculateWeeklyLaborLawMinutes();
            int monthlyMinutes = calculationService.calculateMonthlyLaborLawMinutes();
            
            Map<String, Integer> standards = new HashMap<>();
            standards.put("weeklyMinutes", weeklyMinutes);
            standards.put("monthlyMinutes", monthlyMinutes);
            standards.put("weeklyHours", weeklyMinutes / 60);
            standards.put("monthlyHours", monthlyMinutes / 60);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("노동법 기준 근무 시간 조회 완료", standards));
                            
        } catch (Exception e) {
            log.error("노동법 기준 근무 시간 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 노동법 준수 여부 확인
     */
    @PostMapping("/labor-law-compliance")
    public ResponseEntity<ApiResponse<Boolean>> checkLaborLawCompliance(
            @Valid @RequestBody WorkPolicyRequestDto requestDto) {
        log.info("노동법 준수 여부 확인 요청: {}", requestDto.getName());
        
        try {
            boolean isCompliant = calculationService.isCompliantWithLaborLaw(requestDto);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            isCompliant ? "노동법을 준수합니다." : "노동법을 준수하지 않습니다.", 
                            isCompliant));
                            
        } catch (Exception e) {
            log.error("노동법 준수 여부 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 코어 타임 유효성 검증
     */
    @PostMapping("/validate-core-time")
    public ResponseEntity<ApiResponse<Boolean>> validateCoreTime(
            @RequestParam(required = false) LocalTime coreTimeStart,
            @RequestParam(required = false) LocalTime coreTimeEnd) {
        log.info("코어 타임 유효성 검증 요청: {} ~ {}", coreTimeStart, coreTimeEnd);
        
        try {
            boolean isValid = calculationService.validateCoreTime(coreTimeStart, coreTimeEnd);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            isValid ? "코어 타임이 유효합니다." : "코어 타임이 유효하지 않습니다.", 
                            isValid));
                            
        } catch (Exception e) {
            log.error("코어 타임 유효성 검증 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 시간 범위 유효성 검증
     */
    @PostMapping("/validate-work-time-range")
    public ResponseEntity<ApiResponse<Boolean>> validateWorkTimeRange(
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime) {
        log.info("근무 시간 범위 유효성 검증 요청: {} ~ {}", startTime, endTime);
        
        try {
            boolean isValid = calculationService.validateWorkTimeRange(startTime, endTime);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            isValid ? "근무 시간 범위가 유효합니다." : "근무 시간 범위가 유효하지 않습니다.", 
                            isValid));
                            
        } catch (Exception e) {
            log.error("근무 시간 범위 유효성 검증 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
} 