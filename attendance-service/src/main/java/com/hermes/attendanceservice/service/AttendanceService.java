package com.hermes.attendanceservice.service;

import com.hermes.attendanceservice.dto.AttendanceResponse;
import com.hermes.attendanceservice.dto.WeeklyWorkSummary;
import com.hermes.attendanceservice.entity.WorkStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AttendanceService {
    // 출근
    AttendanceResponse checkIn(Long userId, LocalDateTime checkInTime);
    // 퇴근
    AttendanceResponse checkOut(Long userId, LocalDateTime checkOutTime);

    /** 버튼 없이 상태 지정(연차/출장/외근/재택 등) + 필요 시 자동 시간 기록 */
    AttendanceResponse markStatus(Long userId,
                                  LocalDate date,
                                  WorkStatus status,
                                  boolean autoRecorded,
                                  LocalDateTime checkInTime,
                                  LocalDateTime checkOutTime);

    /** 이번 주(일~토) 요약 */
    WeeklyWorkSummary getThisWeekSummary(Long userId);

    /** 임의 주(weekStart는 일요일로 전달; 일요일이 아니어도 자동 보정) */
    WeeklyWorkSummary getWeekSummary(Long userId, LocalDate weekStartSunday);
}
