package com.hermes.attendanceservice.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import com.hermes.attendanceservice.entity.attendance.AttendanceStatus;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyWorkSummary {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String attendanceStatus; // 출근 상태
    private String workStatus; // 근무 상태
    private Double workHours; // 해당 날짜의 근무 시간 (시간 단위)
    private Double workMinutes; // 해당 날짜의 근무 시간 (분 단위)
    private String checkInTime;
    private String checkOutTime;
    private String workDuration; // 근무 시간 (예: "8시간 30분")
} 
