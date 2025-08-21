package com.hermes.attendanceservice.controller;


import com.hermes.api.common.ApiResponse;

import com.hermes.attendanceservice.dto.AttendanceResponse;
import com.hermes.attendanceservice.dto.WeeklyWorkSummary;
import com.hermes.attendanceservice.dto.WeeklyWorkDetail;
import com.hermes.attendanceservice.dto.WeeklyWorkStats;
import com.hermes.attendanceservice.dto.CheckInRequest;
import com.hermes.attendanceservice.dto.CheckOutRequest;
import com.hermes.attendanceservice.entity.WorkStatus;
import com.hermes.attendanceservice.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ApiResponse<AttendanceResponse> checkIn(@RequestBody CheckInRequest request) {
        try {
            AttendanceResponse response = attendanceService.checkIn(request.getUserId(), request.getCheckIn());
            return ApiResponse.success("출근 기록이 성공적으로 등록되었습니다.", response);
        } catch (Exception e) {
            return ApiResponse.failure("출근 기록 등록에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/check-out")
    public ApiResponse<AttendanceResponse> checkOut(@RequestBody CheckOutRequest request) {
        try {
            AttendanceResponse response = attendanceService.checkOut(request.getUserId(), request.getCheckOut());
            return ApiResponse.success("퇴근 기록이 성공적으로 등록되었습니다.", response);
        } catch (Exception e) {
            return ApiResponse.failure("퇴근 기록 등록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 연차/출장/외근/재택 등 상태 기록 */
    @PostMapping("/status")
    public ApiResponse<AttendanceResponse> markStatus(@RequestParam Long userId,
                                         @RequestParam
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                         LocalDate date,
                                         @RequestParam WorkStatus status,
                                         @RequestParam(defaultValue = "true") boolean autoRecorded,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                         LocalDateTime checkInTime,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                         LocalDateTime checkOutTime) {
        try {
            AttendanceResponse response = attendanceService.markStatus(userId, date, status, autoRecorded, checkInTime, checkOutTime);
            return ApiResponse.success("근무 상태가 성공적으로 기록되었습니다.", response);
        } catch (Exception e) {
            return ApiResponse.failure("근무 상태 기록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 이번 주(일~토) 상세 */
    @GetMapping("/weekly/this")
    public ApiResponse<WeeklyWorkDetail> getThisWeek(@RequestParam Long userId) {
        try {
            WeeklyWorkSummary summary = attendanceService.getThisWeekSummary(userId);
            
            WeeklyWorkDetail detail = WeeklyWorkDetail.builder()
                    .userId(summary.getUserId())
                    .weekStart(summary.getWeekStart())
                    .weekEnd(summary.getWeekEnd())
                    .dailySummaries(summary.getDailySummaries())
                    .build();
            
            return ApiResponse.success("이번 주 근무 상세를 성공적으로 조회했습니다.", detail);
        } catch (Exception e) {
            return ApiResponse.failure("이번 주 근무 상세 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /** 임의 주(weekStart가 일요일이 아니어도 자동 보정) */
    @GetMapping("/weekly")
    public ApiResponse<WeeklyWorkDetail> getWeek(@RequestParam Long userId,
                                     @RequestParam
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                     LocalDate weekStart) {
        try {
            WeeklyWorkSummary summary = attendanceService.getWeekSummary(userId, weekStart);
            
            WeeklyWorkDetail detail = WeeklyWorkDetail.builder()
                    .userId(summary.getUserId())
                    .weekStart(summary.getWeekStart())
                    .weekEnd(summary.getWeekEnd())
                    .dailySummaries(summary.getDailySummaries())
                    .build();
            
            return ApiResponse.success("주간 근무 상세를 성공적으로 조회했습니다.", detail);
        } catch (Exception e) {
            return ApiResponse.failure("주간 근무 상세 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /** 주간 근무 통계 (간단한 통계 정보만) */
    @GetMapping("/weekly/stats")
    public ApiResponse<WeeklyWorkStats> getWeeklyStats(@RequestParam Long userId,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                       LocalDate weekStart) {
        try {
            LocalDate targetWeekStart = weekStart != null ? weekStart : 
                LocalDate.now(ZoneId.of("Asia/Seoul")).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            
            WeeklyWorkSummary summary = attendanceService.getWeekSummary(userId, targetWeekStart);
            
            WeeklyWorkStats stats = WeeklyWorkStats.builder()
                    .totalWorkHours(summary.getTotalWorkHours())
                    .totalWorkMinutes(summary.getTotalWorkMinutes())
                    .workDays(summary.getWorkDays())
                    .regularWorkHours(summary.getRegularWorkHours())
                    .lateWorkHours(summary.getLateWorkHours())
                    .overtimeHours(summary.getOvertimeHours())
                    .vacationHours(summary.getVacationHours())
                    .build();
            
            return ApiResponse.success("주간 근무 통계를 성공적으로 조회했습니다.", stats);
        } catch (Exception e) {
            return ApiResponse.failure("주간 근무 통계 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /** 이번 주 근무 통계 (간단한 통계 정보만) */
    @GetMapping("/weekly/this/stats")
    public ApiResponse<WeeklyWorkStats> getThisWeekStats(@RequestParam Long userId) {
        try {
            WeeklyWorkSummary summary = attendanceService.getThisWeekSummary(userId);
            
            WeeklyWorkStats stats = WeeklyWorkStats.builder()
                    .totalWorkHours(summary.getTotalWorkHours())
                    .totalWorkMinutes(summary.getTotalWorkMinutes())
                    .workDays(summary.getWorkDays())
                    .regularWorkHours(summary.getRegularWorkHours())
                    .lateWorkHours(summary.getLateWorkHours())
                    .overtimeHours(summary.getOvertimeHours())
                    .vacationHours(summary.getVacationHours())
                    .build();
            
            return ApiResponse.success("이번 주 근무 통계를 성공적으로 조회했습니다.", stats);
        } catch (Exception e) {
            return ApiResponse.failure("이번 주 근무 통계 조회에 실패했습니다: " + e.getMessage());
        }
    }
}
