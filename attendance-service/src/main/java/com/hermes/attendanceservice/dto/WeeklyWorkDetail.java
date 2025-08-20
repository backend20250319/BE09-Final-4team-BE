package com.hermes.attendanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyWorkDetail extends WeeklyWorkBase {
    private List<DailyWorkSummary> dailySummaries; // 일별 요약
} 