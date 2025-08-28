package com.hermes.attendanceservice.service.attendance;

import com.hermes.attendanceservice.dto.attendance.AttendanceResponse;
import com.hermes.attendanceservice.dto.attendance.WeeklyWorkSummary;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AttendanceService {
    // 출근
    AttendanceResponse checkIn(Long userId, LocalDateTime checkInTime);
    // 퇴근
    AttendanceResponse checkOut(Long userId, LocalDateTime checkOutTime);

    /** 버튼 클릭 상태 지정(휴가/출장/재택/택시 등) + 필요시 자동 시간 기록 */
    AttendanceResponse markStatus(Long userId,
                                  LocalDate date,
                                  WorkStatus status,
                                  boolean autoRecorded,
                                  LocalDateTime checkInTime,
                                  LocalDateTime checkOutTime);

    /** 이번 주 근무 요약 */
    WeeklyWorkSummary getThisWeekSummary(Long userId);

    /** 특정 주 (weekStart가 요일로 입력; 요일이 아니어도 자동 보정) */
    WeeklyWorkSummary getWeekSummary(Long userId, LocalDate weekStartSunday);
} 