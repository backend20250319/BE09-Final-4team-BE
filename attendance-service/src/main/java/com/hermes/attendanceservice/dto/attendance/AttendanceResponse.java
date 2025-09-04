package com.hermes.attendanceservice.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hermes.attendanceservice.entity.attendance.AttendanceStatus;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
public class AttendanceResponse {

    private Long id;        // 출퇴근 PK
    private Long userId;    // 사용자 ID
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date; // 근무 날짜

    private Instant checkIn; // 출근 시간 (UTC Instant)

    private Instant checkOut; // 퇴근 시간 (UTC Instant)

    private AttendanceStatus attendanceStatus; // 출근 상태 (REGULAR, LATE, NOT_CLOCKIN, ABSENT)
    private WorkStatus workStatus; // 근무 상태 (OFFICE, REMOTE, VACATION, BUSINESS_TRIP 등)

    private boolean autoRecorded; // 자동 기록 여부
} 
