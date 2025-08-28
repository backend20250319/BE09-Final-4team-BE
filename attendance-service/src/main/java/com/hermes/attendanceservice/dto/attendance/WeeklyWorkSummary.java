package com.hermes.attendanceservice.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyWorkSummary extends WeeklyWorkBase {
    private double totalWorkHours; // 총 근무시간 (시간 단위)
    private double totalWorkMinutes; // 총 근무시간 (분 단위)
    private int workDays; // 근무한 날짜 수
    
    // 근무 상태별 시간
    private double regularWorkHours; // 정상 근무 시간
    private double lateWorkHours; // 지각 근무 시간
    private double overtimeHours; // 초과 근무 시간
    private double vacationHours; // 휴가 시간
    
    private List<DailyWorkSummary> dailySummaries; // 일별 요약
} 
