package com.hermes.attendanceservice.dto.workpolicy;

import lombok.*;

import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualLeaveUpdateDto {
    
    @Size(max = 100, message = "연차 이름은 100자를 초과할 수 없습니다.")
    private String name;
    
    @Min(value = 0, message = "최소 근무년수는 0 이상이어야 합니다.")
    private Integer minYears;
    
    @Min(value = 0, message = "최대 근무년수는 0 이상이어야 합니다.")
    private Integer maxYears;
    
    @Min(value = 1, message = "연차 일수는 1 이상이어야 합니다.")
    @Max(value = 365, message = "연차 일수는 365 이하여야 합니다.")
    private Integer leaveDays;
    
    @Min(value = 0, message = "휴일 일수는 0 이상이어야 합니다.")
    private Integer holidayDays;
} 
