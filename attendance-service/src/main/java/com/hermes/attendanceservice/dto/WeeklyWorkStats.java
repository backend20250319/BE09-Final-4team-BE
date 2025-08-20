package com.hermes.attendanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyWorkStats {
    private double totalWorkHours;
    private double totalWorkMinutes;
    private int workDays;
    private double regularWorkHours;
    private double lateWorkHours;
    private double overtimeHours;
    private double vacationHours;
} 