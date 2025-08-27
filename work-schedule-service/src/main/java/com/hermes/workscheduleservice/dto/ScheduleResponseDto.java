package com.hermes.workscheduleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String scheduleType;
    private String color;
    private Boolean isAllDay;
    private Boolean isRecurring;
    private String recurrencePattern;
    private Integer recurrenceInterval;
    private List<String> recurrenceDays;
    private LocalDate recurrenceEndDate;
    private Long workPolicyId;
    private Integer priority;
    private String location;
    private List<String> attendees;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status; // ACTIVE, CANCELLED, COMPLETED
    private Boolean isFixed; // 고정 스케줄 여부
    private Boolean isEditable; // 편집 가능 여부
    private String fixedReason; // 고정 사유
} 