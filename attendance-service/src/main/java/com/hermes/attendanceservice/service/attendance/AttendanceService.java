package com.hermes.attendanceservice.service.attendance;

import com.hermes.attendanceservice.dto.attendance.AttendanceResponse;
import com.hermes.attendanceservice.dto.attendance.WeeklyWorkSummary;
import com.hermes.attendanceservice.entity.attendance.AttendanceStatus;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public interface AttendanceService {
    // 출근
    AttendanceResponse checkIn(Long userId, LocalDateTime checkInTime);
    // 퇴근
    AttendanceResponse checkOut(Long userId, LocalDateTime checkOutTime);

    /** 출근 상태 기록 */
    AttendanceResponse markAttendanceStatus(Long userId,
                                           LocalDate date,
                                           AttendanceStatus attendanceStatus,
                                           boolean autoRecorded,
                                           LocalDateTime checkInTime,
                                           LocalDateTime checkOutTime);

    /** 근무 상태 기록 */
    AttendanceResponse markWorkStatus(Long userId,
                                     LocalDate date,
                                     WorkStatus workStatus,
                                     boolean autoRecorded,
                                     LocalDateTime checkInTime,
                                     LocalDateTime checkOutTime);

    /** 이번 주 근무 요약 */
    WeeklyWorkSummary getThisWeekSummary(Long userId);

    /** 특정 주 (weekStart가 요일로 입력; 요일이 아니어도 자동 보정) */
    WeeklyWorkSummary getWeekSummary(Long userId, LocalDate weekStartSunday);
    
    /** 출근 가능 시간 조회 */
    Map<String, Object> getCheckInAvailableTime(Long userId);
} 