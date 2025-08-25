package com.hermes.workpolicyservice.dto;

import com.hermes.workpolicyservice.entity.WorkType;
import lombok.*;

import java.time.LocalDateTime;

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
    private Integer totalHolidayDays; // 총 휴가 일수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 계산된 필드들
    private Integer totalWorkMinutes;
    private Boolean isCompliantWithLaborLaw;
} 