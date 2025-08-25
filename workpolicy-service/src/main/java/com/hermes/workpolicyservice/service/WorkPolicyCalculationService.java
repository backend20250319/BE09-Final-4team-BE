package com.hermes.workpolicyservice.service;

import com.hermes.workpolicyservice.dto.WorkPolicyRequestDto;
import com.hermes.workpolicyservice.entity.WorkPolicy;

import java.time.LocalTime;

public interface WorkPolicyCalculationService {
    
    /**
     * 총 근무 시간을 분 단위로 계산
     */
    int calculateTotalWorkMinutes(int workHours, int workMinutes);
    
    /**
     * 노동법 기준 근무 시간 계산 (주 기준)
     */
    int calculateWeeklyLaborLawMinutes();
    
    /**
     * 노동법 기준 근무 시간 계산 (월 기준)
     */
    int calculateMonthlyLaborLawMinutes();
    
    /**
     * 근무 정책의 노동법 준수 여부 확인
     */
    boolean isCompliantWithLaborLaw(WorkPolicy workPolicy);
    
    /**
     * 근무 정책 요청의 노동법 준수 여부 확인
     */
    boolean isCompliantWithLaborLaw(WorkPolicyRequestDto requestDto);
    
    /**
     * 코어 타임 유효성 검증
     */
    boolean validateCoreTime(LocalTime coreTimeStart, LocalTime coreTimeEnd);
    
    /**
     * 근무 시간 범위 유효성 검증
     */
    boolean validateWorkTimeRange(LocalTime startTime, LocalTime endTime);
    
    /**
     * 주간 근무일 수 계산
     */
    int calculateWeeklyWorkingDays(WorkPolicy workPolicy);
    
    /**
     * 월간 근무일 수 계산
     */
    int calculateMonthlyWorkingDays(WorkPolicy workPolicy);
    
    /**
     * 연간 근무일 수 계산
     */
    int calculateAnnualWorkingDays(WorkPolicy workPolicy);
} 