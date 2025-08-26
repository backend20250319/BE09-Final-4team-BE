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
    private LocalTime avgWorkTime;
    private Integer totalRequiredMinutes;
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
} 
