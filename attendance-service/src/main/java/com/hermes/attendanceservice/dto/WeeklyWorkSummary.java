package com.hermes.attendanceservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WeeklyWorkSummary {
    
    private Long userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart; // 주의 시작일 (일요일)
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnd; // 주의 종료일 (토요일)
    
    private double totalWorkHours; // 총 근무시간 (시간 단위)
    private double totalWorkMinutes; // 총 근무시간 (분 단위)
    private int workDays; // 근무한 날짜 수
    
    // 근무 상태별 시간
    private double regularWorkHours; // 정상 근무 시간
    private double lateWorkHours; // 지각 후 근무 시간
    private double overtimeHours; // 초과 근무 시간
    private double vacationHours; // 연차 시간
    
    private List<DailyWorkSummary> dailySummaries; // 일별 요약
    
    @Data
    @Builder
    public static class DailyWorkSummary {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private String status;
        private Double workHours; // 해당 날짜의 근무 시간 (시간 단위)
        private Double workMinutes; // 해당 날짜의 근무 시간 (분 단위)
        private String checkInTime;
        private String checkOutTime;
        private String workDuration; // 근무 시간 (예: "8시간 30분")
    }
} 