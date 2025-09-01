package com.hermes.attendanceservice.dto.workschedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.hermes.attendanceservice.entity.workschedule.ScheduleType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequestDto {
    
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
    
    @NotBlank(message = "스케줄 제목은 필수입니다.")
    private String title;
    
    private String description;
    
    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;
    
    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;
    
    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalTime startTime;
    
    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalTime endTime;
    
    @NotNull(message = "스케줄 유형은 필수입니다.")
    private ScheduleType scheduleType; // WORK(근무), SICK_LEAVE(병가), VACATION(휴가), BUSINESS_TRIP(출장), OUT_OF_OFFICE(외근), OVERTIME(초과근무)
    
    private String color;
    
    @Builder.Default
    private Boolean isAllDay = false;
    
    @Builder.Default
    private Boolean isRecurring = false;
    
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY, YEARLY
    
    @Builder.Default
    private Integer recurrenceInterval = 1;
    
    private List<String> recurrenceDays; // MONDAY, TUESDAY, etc.
    
    private LocalDate recurrenceEndDate;
    
    private Long workPolicyId; // 기존 근무 정책과 연동할 경우
    
    @Min(value = 0, message = "우선순위는 0 이상이어야 합니다.")
    @Max(value = 10, message = "우선순위는 10 이하여야 합니다.")
    @Builder.Default
    private Integer priority = 5;
    
    private String location;
    
    private List<String> attendees; // 참석자 이메일 리스트
    
    private String notes;
} 