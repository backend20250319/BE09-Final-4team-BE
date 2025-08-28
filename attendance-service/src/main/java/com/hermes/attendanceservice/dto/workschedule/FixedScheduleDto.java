package com.hermes.attendanceservice.dto.workschedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixedScheduleDto {
    private Long id;
    private Long userId;
    private Long workPolicyId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String scheduleType; // WORK, BREAK, HOLIDAY
    private String color;
    private Boolean isAllDay;
    private Boolean isRecurring;
    private String recurrencePattern;
    private Integer recurrenceInterval;
    private String recurrenceDays;
    @Builder.Default
    private Boolean isFixed = true; // 고정 스케줄 여부
    @Builder.Default
    private Boolean isEditable = false; // 편집 가능 여부
    private String fixedReason; // 고정 사유 (WORK_POLICY, HOLIDAY, BREAK_TIME)
} 