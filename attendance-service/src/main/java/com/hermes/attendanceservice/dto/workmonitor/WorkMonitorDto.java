package com.hermes.attendanceservice.dto.workmonitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkMonitorDto {
    
    private Long id;
    private LocalDate date;
    private Integer totalEmployees;
    private Integer attendanceCount;
    private Integer lateCount;
    private Integer vacationCount;
} 