package com.hermes.attendanceservice.dto.workpolicy;

import com.hermes.attendanceservice.entity.workpolicy.WorkType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPolicyListResponseDto {
    
    private Long id;
    private String name;
    private WorkType type;
    private Integer workHours;
    private Integer workMinutes;
    private Integer totalRequiredMinutes;
    private Integer totalAnnualLeaveDays; // 총 연차 일수
    private Integer totalHolidayDays; // 총 휴일 일수
    private Instant createdAt;
    private Instant updatedAt;
    
    // 계산된 필드들
    private Integer totalWorkMinutes;
    private Boolean isCompliantWithLaborLaw;
} 
