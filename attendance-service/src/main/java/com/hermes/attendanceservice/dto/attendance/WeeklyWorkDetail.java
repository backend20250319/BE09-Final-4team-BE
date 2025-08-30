package com.hermes.attendanceservice.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyWorkDetail extends WeeklyWorkBase {
    private List<DailyWorkSummary> dailySummaries; // 일별 요약
} 
