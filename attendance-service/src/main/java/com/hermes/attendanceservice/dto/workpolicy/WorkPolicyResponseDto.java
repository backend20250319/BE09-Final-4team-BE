package com.hermes.attendanceservice.dto.workpolicy;

import com.hermes.attendanceservice.entity.workpolicy.StartDayOfWeek;
import com.hermes.attendanceservice.entity.workpolicy.WorkCycle;
import com.hermes.attendanceservice.entity.workpolicy.WorkType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPolicyResponseDto {
    
    private Long id;
    private String name;
    private WorkType type;
    private WorkCycle workCycle;
    private StartDayOfWeek startDayOfWeek;
    private Integer workCycleStartDay;
    private List<StartDayOfWeek> workDays;
    private Integer weeklyWorkingDays;
    private LocalTime startTime;
    private LocalTime startTimeEnd;
    private Integer workHours;
    private Integer workMinutes;
    private LocalTime coreTimeStart;
    private LocalTime coreTimeEnd;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private Integer breakMinutes;
    private LocalTime avgWorkTime;
    private Integer totalRequiredMinutes;
    private List<StartDayOfWeek> holidayDays;
    private List<String> holidays;
    private Boolean isHolidayFixed;
    private Boolean isBreakFixed;
    private List<AnnualLeaveResponseDto> annualLeaves;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 계산된 필드들
    private Integer totalWorkMinutes;
    private Boolean isCompliantWithLaborLaw;
    private Boolean isOptionalWork;
    private Boolean isShiftWork;
    private Boolean isFlexibleWork;
    private Boolean isFixedWork;
    
    public static WorkPolicyResponseDto from(com.hermes.attendanceservice.entity.workpolicy.WorkPolicy workPolicy) {
        return WorkPolicyResponseDto.builder()
                .id(workPolicy.getId())
                .name(workPolicy.getName())
                .type(workPolicy.getType())
                .workCycle(workPolicy.getWorkCycle())
                .startDayOfWeek(workPolicy.getStartDayOfWeek())
                .workCycleStartDay(workPolicy.getWorkCycleStartDay())
                .workDays(workPolicy.getWorkDays())
                .weeklyWorkingDays(workPolicy.getWeeklyWorkingDays())
                .startTime(workPolicy.getStartTime())
                .startTimeEnd(workPolicy.getStartTimeEnd())
                .workHours(workPolicy.getWorkHours())
                .workMinutes(workPolicy.getWorkMinutes())
                .coreTimeStart(workPolicy.getCoreTimeStart())
                .coreTimeEnd(workPolicy.getCoreTimeEnd())
                .breakStartTime(workPolicy.getBreakStartTime())
                .breakEndTime(workPolicy.getBreakEndTime())
                .breakMinutes(workPolicy.getBreakMinutes())
                .avgWorkTime(workPolicy.getAvgWorkTime())
                .totalRequiredMinutes(workPolicy.getTotalRequiredMinutes())
                .holidayDays(workPolicy.getHolidayDays())
                .holidays(workPolicy.getHolidays())
                .isHolidayFixed(workPolicy.getIsHolidayFixed())
                .isBreakFixed(workPolicy.getIsBreakFixed())
                .createdAt(workPolicy.getCreatedAt())
                .updatedAt(workPolicy.getUpdatedAt())
                .totalWorkMinutes(workPolicy.getTotalWorkMinutes())
                .isCompliantWithLaborLaw(workPolicy.isCompliantWithLaborLaw())
                .isOptionalWork(workPolicy.isOptionalWork())
                .isShiftWork(workPolicy.isShiftWork())
                .isFlexibleWork(workPolicy.isFlexibleWork())
                .isFixedWork(workPolicy.isFixedWork())
                .build();
    }
} 
