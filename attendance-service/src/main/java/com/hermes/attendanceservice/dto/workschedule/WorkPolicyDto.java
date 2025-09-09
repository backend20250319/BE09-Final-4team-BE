package com.hermes.attendanceservice.dto.workschedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkPolicyDto {
    private Long id;
    private String name;
    private String type; // FIXED, SHIFT, FLEXIBLE, OPTIONAL
    private String workCycle; // ONE_WEEK, TWO_WEEK, THREE_WEEK, FOUR_WEEK, ONE_MONTH
    private String startDayOfWeek; // MONDAY, TUESDAY, etc.
    private Integer workCycleStartDay;
    private List<String> workDays; // 근무 요일 리스트
    private List<String> holidayDays; // 휴일 요일 리스트 (SATURDAY, SUNDAY 등)
    private Integer weeklyWorkingDays;
    private LocalTime startTime; // 근무 시작 시간
    private LocalTime startTimeEnd; // 근무 시작 시간 종료  
    private LocalTime endTime; // 근무 종료 시간
    private Integer workHours;
    private Integer workMinutes;
    private LocalTime coreTimeStart;
    private LocalTime coreTimeEnd;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime; // 휴식 종료 시간
    private Integer breakMinutes; // 휴식 시간 (분)
    private LocalTime avgWorkTime;
    private Integer totalRequiredMinutes;
    private List<String> holidays; // 공휴일 리스트 (예: ["2024-01-01", "2024-01-02"])
    @Builder.Default
    private Boolean isHolidayFixed = true; // 휴일 고정 여부
    @Builder.Default
    private Boolean isBreakFixed = true; // 휴식 시간 고정 여부
} 