package com.hermes.attendanceservice.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceResponse {

    private Long id;        // 출퇴근 PK
    private Long userId;    // 사용자 ID
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date; // 근무 날짜

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn; // 출근 시간

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut; // 퇴근 시간

    private WorkStatus status; // 근무 상태 (REGULAR, LATE, VACATION 등)

    private boolean autoRecorded; // 자동 기록 여부
} 
