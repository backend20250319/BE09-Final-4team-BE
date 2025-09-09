package com.hermes.attendanceservice.dto.workpolicy;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualLeaveResponseDto {
    
    private Long id;
    private Long workPolicyId;
    private String name;
    private Integer minYears;
    private Integer maxYears;
    private Integer leaveDays;
    private String rangeDescription; // 범위 설명 (예: "0~1년차", "2~3년차")
    private Instant createdAt;
    private Instant updatedAt;
} 
