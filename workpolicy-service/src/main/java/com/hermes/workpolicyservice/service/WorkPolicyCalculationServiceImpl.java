package com.hermes.workpolicyservice.service;

import com.hermes.workpolicyservice.dto.WorkPolicyRequestDto;
import com.hermes.workpolicyservice.entity.StartDayOfWeek;
import com.hermes.workpolicyservice.entity.WorkPolicy;
import com.hermes.workpolicyservice.entity.WorkCycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkPolicyCalculationServiceImpl implements WorkPolicyCalculationService {
    
    // 노동법 기준 상수
    private static final int WEEKLY_LABOR_LAW_HOURS = 40; // 주 40시간
    private static final int MONTHLY_LABOR_LAW_HOURS = 160; // 월 160시간
    private static final int MINUTES_PER_HOUR = 60;
    
    @Override
    public int calculateTotalWorkMinutes(int workHours, int workMinutes) {
        return (workHours * MINUTES_PER_HOUR) + workMinutes;
    }
    
    @Override
    public int calculateWeeklyLaborLawMinutes() {
        return WEEKLY_LABOR_LAW_HOURS * MINUTES_PER_HOUR; // 2400분
    }
    
    @Override
    public int calculateMonthlyLaborLawMinutes() {
        return MONTHLY_LABOR_LAW_HOURS * MINUTES_PER_HOUR; // 9600분
    }
    
    @Override
    public boolean isCompliantWithLaborLaw(WorkPolicy workPolicy) {
        log.debug("노동법 준수 여부 확인: 정책={}, 총 필요 분={}", 
                workPolicy.getName(), workPolicy.getTotalRequiredMinutes());
        
        WorkCycle workCycle = workPolicy.getWorkCycle();
        int totalRequiredMinutes = workPolicy.getTotalRequiredMinutes();
        
        if (workCycle == WorkCycle.ONE_MONTH) {
            boolean isCompliant = totalRequiredMinutes <= calculateMonthlyLaborLawMinutes();
            log.debug("월 기준 노동법 준수 여부: {} ({}분 <= {}분)", 
                    isCompliant, totalRequiredMinutes, calculateMonthlyLaborLawMinutes());
            return isCompliant;
        } else {
            boolean isCompliant = totalRequiredMinutes <= calculateWeeklyLaborLawMinutes();
            log.debug("주 기준 노동법 준수 여부: {} ({}분 <= {}분)", 
                    isCompliant, totalRequiredMinutes, calculateWeeklyLaborLawMinutes());
            return isCompliant;
        }
    }
    
    @Override
    public boolean isCompliantWithLaborLaw(WorkPolicyRequestDto requestDto) {
        log.debug("노동법 준수 여부 확인: 정책={}, 총 필요 분={}", 
                requestDto.getName(), requestDto.getTotalRequiredMinutes());
        
        WorkCycle workCycle = requestDto.getWorkCycle();
        Integer totalRequiredMinutes = requestDto.getTotalRequiredMinutes();
        
        if (totalRequiredMinutes == null) {
            log.warn("총 필요 분이 설정되지 않았습니다.");
            return false;
        }
        
        if (workCycle == WorkCycle.ONE_MONTH) {
            boolean isCompliant = totalRequiredMinutes <= calculateMonthlyLaborLawMinutes();
            log.debug("월 기준 노동법 준수 여부: {} ({}분 <= {}분)", 
                    isCompliant, totalRequiredMinutes, calculateMonthlyLaborLawMinutes());
            return isCompliant;
        } else {
            boolean isCompliant = totalRequiredMinutes <= calculateWeeklyLaborLawMinutes();
            log.debug("주 기준 노동법 준수 여부: {} ({}분 <= {}분)", 
                    isCompliant, totalRequiredMinutes, calculateWeeklyLaborLawMinutes());
            return isCompliant;
        }
    }
    
    @Override
    public boolean validateCoreTime(LocalTime coreTimeStart, LocalTime coreTimeEnd) {
        if (coreTimeStart == null || coreTimeEnd == null) {
            log.debug("코어 타임이 설정되지 않았습니다.");
            return true; // 선택 근무에서 코어 타임은 선택사항
        }
        
        boolean isValid = !coreTimeStart.isAfter(coreTimeEnd);
        log.debug("코어 타임 유효성 검증: {} ({} ~ {})", isValid, coreTimeStart, coreTimeEnd);
        return isValid;
    }
    
    @Override
    public boolean validateWorkTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            log.debug("근무 시간 범위가 설정되지 않았습니다.");
            return true; // 일부 근무 유형에서는 선택사항
        }
        
        boolean isValid = !startTime.isAfter(endTime);
        log.debug("근무 시간 범위 유효성 검증: {} ({} ~ {})", isValid, startTime, endTime);
        return isValid;
    }
    
    @Override
    public int calculateWeeklyWorkingDays(WorkPolicy workPolicy) {
        List<StartDayOfWeek> workDays = workPolicy.getWorkDays();
        if (workDays == null || workDays.isEmpty()) {
            log.warn("근무 요일이 설정되지 않았습니다.");
            return 0;
        }
        
        int weeklyDays = workDays.size();
        log.debug("주간 근무일 수 계산: {}일", weeklyDays);
        return weeklyDays;
    }
    
    @Override
    public int calculateMonthlyWorkingDays(WorkPolicy workPolicy) {
        int weeklyWorkingDays = calculateWeeklyWorkingDays(workPolicy);
        if (weeklyWorkingDays == 0) {
            return 0;
        }
        
        // 평균 월 4.33주 기준으로 계산
        int monthlyDays = (int) Math.round(weeklyWorkingDays * 4.33);
        log.debug("월간 근무일 수 계산: {}일 (주간 {}일 기준)", monthlyDays, weeklyWorkingDays);
        return monthlyDays;
    }
    
    @Override
    public int calculateAnnualWorkingDays(WorkPolicy workPolicy) {
        int weeklyWorkingDays = calculateWeeklyWorkingDays(workPolicy);
        if (weeklyWorkingDays == 0) {
            return 0;
        }
        
        // 연 52주 기준으로 계산
        int annualDays = weeklyWorkingDays * 52;
        log.debug("연간 근무일 수 계산: {}일 (주간 {}일 기준)", annualDays, weeklyWorkingDays);
        return annualDays;
    }
} 