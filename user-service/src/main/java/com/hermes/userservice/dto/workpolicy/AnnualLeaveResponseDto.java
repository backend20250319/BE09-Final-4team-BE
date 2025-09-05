package com.hermes.userservice.dto.workpolicy;

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
    private Integer holidayDays;
    private String rangeDescription;
    private Instant createdAt;
    private Instant updatedAt;
}
