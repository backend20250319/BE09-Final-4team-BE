package com.hermes.attendanceservice.dto.workschedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColleagueScheduleResponseDto {
    private Long colleagueId;
    private String colleagueName;
    private String colleaguePosition;
    private String colleagueDepartment;
    private String colleagueAvatar;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyScheduleDto> dailySchedules;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyScheduleDto {
        private LocalDate date;
        private String dayOfWeek; // "Mon", "Tue", etc.
        private List<ScheduleEventDto> events;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleEventDto {
        private Long scheduleId;
        private LocalTime startTime;
        private LocalTime endTime;
        private String scheduleType; // WORK, BREAK, EXTERNAL_WORK, BUSINESS_TRIP, WORK_FROM_HOME
    }
} 