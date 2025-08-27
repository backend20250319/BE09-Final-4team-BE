package com.hermes.attendanceservice.dto.workpolicy;

import com.hermes.attendanceservice.entity.workpolicy.StartDayOfWeek;
import com.hermes.attendanceservice.entity.workpolicy.WorkCycle;
import com.hermes.attendanceservice.entity.workpolicy.WorkType;
import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPolicyRequestDto {
    
    @NotBlank(message = "근무 정책 이름은 필수입니다.")
    @Size(max = 100, message = "근무 정책 이름은 100자를 초과할 수 없습니다.")
    private String name;
    
    @NotNull(message = "근무 타입은 필수입니다.")
    private WorkType type;
    
    private WorkCycle workCycle; // 탄력 근무 용도
    
    @NotNull(message = "근무 시작 요일은 필수입니다.")
    private StartDayOfWeek startDayOfWeek;
    
    @Min(value = 1, message = "근무 주기 시작일은 1 이상이어야 합니다.")
    @Max(value = 31, message = "근무 주기 시작일은 31 이하여야 합니다.")
    private Integer workCycleStartDay; // 탄력 근무 용도
    
    @NotEmpty(message = "근무 요일은 필수입니다.")
    private List<StartDayOfWeek> workDays;
    
    @Min(value = 1, message = "주간 근무일은 1 이상이어야 합니다.")
    @Max(value = 7, message = "주간 근무일은 7 이하여야 합니다.")
    private Integer weeklyWorkingDays; // 교대 근무 용도
    
    private LocalTime startTime;
    
    private LocalTime startTimeEnd; // 교대 근무용
    
    @NotNull(message = "근무 시간은 필수입니다.")
    @Min(value = 0, message = "근무 시간은 0 이상이어야 합니다.")
    @Max(value = 24, message = "근무 시간은 24 이하여야 합니다.")
    private Integer workHours;
    
    @NotNull(message = "근무 분은 필수입니다.")
    @Min(value = 0, message = "근무 분은 0 이상이어야 합니다.")
    @Max(value = 59, message = "근무 분은 59 이하여야 합니다.")
    private Integer workMinutes;
    
    private LocalTime coreTimeStart; // 선택 근무 용도
    
    private LocalTime coreTimeEnd; // 선택 근무 용도
    
    @NotNull(message = "휴게 시작 시간은 필수입니다.")
    private LocalTime breakStartTime;
    
    private LocalTime avgWorkTime; // 선택 근무 용도
    
    @NotNull(message = "총 필요 분은 필수입니다.")
    @Min(value = 1, message = "총 필요 분은 1 이상이어야 합니다.")
    private Integer totalRequiredMinutes;
    
    private List<AnnualLeaveRequestDto> annualLeaves; // 연차 목록
} 
