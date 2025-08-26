package com.hermes.userservice.dto.workpolicy;

import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPolicyUpdateDto {
    
    @Size(max = 100, message = "근무 정책 이름은 100자를 초과할 수 없습니다.")
    private String name;
    
    private String type;
    private String workCycle;
    private String startDayOfWeek;
    
    @Min(value = 1, message = "근무 주기 시작일은 1 이상이어야 합니다.")
    @Max(value = 31, message = "근무 주기 시작일은 31 이하여야 합니다.")
    private Integer workCycleStartDay;
    
    private List<String> workDays;
    
    @Min(value = 1, message = "주간 근무일 수는 1 이상이어야 합니다.")
    @Max(value = 7, message = "주간 근무일 수는 7 이하여야 합니다.")
    private Integer weeklyWorkingDays;
    
    private LocalTime startTime;
    private LocalTime startTimeEnd;
    
    @Min(value = 0, message = "근무 시간은 0 이상이어야 합니다.")
    @Max(value = 24, message = "근무 시간은 24 이하여야 합니다.")
    private Integer workHours;
    
    @Min(value = 0, message = "근무 분은 0 이상이어야 합니다.")
    @Max(value = 59, message = "근무 분은 59 이하여야 합니다.")
    private Integer workMinutes;
    
    private LocalTime coreTimeStart;
    private LocalTime coreTimeEnd;
    private LocalTime breakStartTime;
    private LocalTime avgWorkTime;
    
    @Min(value = 1, message = "총 필요 분은 1 이상이어야 합니다.")
    private Integer totalRequiredMinutes;
    
    private List<AnnualLeaveRequestDto> annualLeaves; // 연차 목록
}
