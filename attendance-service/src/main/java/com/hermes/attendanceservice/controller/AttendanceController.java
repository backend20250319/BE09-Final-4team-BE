package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.attendance.AttendanceResponse;
import com.hermes.attendanceservice.dto.attendance.WeeklyWorkSummary;
import com.hermes.attendanceservice.dto.attendance.WeeklyWorkDetail;
import com.hermes.attendanceservice.dto.attendance.CheckInRequest;
import com.hermes.attendanceservice.dto.attendance.CheckOutRequest;
import com.hermes.attendanceservice.entity.attendance.AttendanceStatus;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;
import com.hermes.attendanceservice.service.attendance.AttendanceService;
import com.hermes.auth.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ApiResult<AttendanceResponse> checkIn(
            @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인만 출근 기록 가능
            if (!user.getUserId().equals(request.getUserId())) {
                return ApiResult.failure("권한이 없습니다.");
            }
            
            AttendanceResponse response = attendanceService.checkIn(request.getUserId(), request.getCheckIn());
            return ApiResult.success("출근 기록이 성공적으로 등록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("출근 기록 등록에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/check-out")
    public ApiResult<AttendanceResponse> checkOut(
            @RequestBody CheckOutRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인만 퇴근 기록 가능
            if (!user.getUserId().equals(request.getUserId())) {
                return ApiResult.failure("권한이 없습니다.");
            }
            
            AttendanceResponse response = attendanceService.checkOut(request.getUserId(), request.getCheckOut());
            return ApiResult.success("퇴근 기록이 성공적으로 등록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("퇴근 기록 등록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 출근 상태 기록 */
    @PostMapping("/attendance-status")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ApiResult<AttendanceResponse> markAttendanceStatus(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam AttendanceStatus attendanceStatus,
            @RequestParam(defaultValue = "true") boolean autoRecorded,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime) {
        try {
            AttendanceResponse response = attendanceService.markAttendanceStatus(userId, date, attendanceStatus, autoRecorded, checkInTime, checkOutTime);
            return ApiResult.success("출근 상태가 성공적으로 기록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("출근 상태 기록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 근무 상태 기록 */
    @PostMapping("/work-status")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ApiResult<AttendanceResponse> markWorkStatus(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam WorkStatus workStatus,
            @RequestParam(defaultValue = "true") boolean autoRecorded,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime) {
        try {
            AttendanceResponse response = attendanceService.markWorkStatus(userId, date, workStatus, autoRecorded, checkInTime, checkOutTime);
            return ApiResult.success("근무 상태가 성공적으로 기록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("근무 상태 기록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 이번 주 근무 상세 */
    @GetMapping("/weekly/this")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ApiResult<WeeklyWorkDetail> getThisWeek(@RequestParam Long userId) {
        try {
            WeeklyWorkSummary summary = attendanceService.getThisWeekSummary(userId);
            
            WeeklyWorkDetail detail = WeeklyWorkDetail.builder()
                    .userId(summary.getUserId())
                    .weekStart(summary.getWeekStart())
                    .weekEnd(summary.getWeekEnd())
                    .dailySummaries(summary.getDailySummaries())
                    .build();
            
            return ApiResult.success("이번 주 근무 상세를 성공적으로 조회했습니다.", detail);
        } catch (Exception e) {
            return ApiResult.failure("이번 주 근무 상세 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /** 특정 주 (weekStart가 요일이 아니어도 자동 보정) */
    @GetMapping("/weekly")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ApiResult<WeeklyWorkDetail> getWeek(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            WeeklyWorkSummary summary = attendanceService.getWeekSummary(userId, weekStart);
            
            WeeklyWorkDetail detail = WeeklyWorkDetail.builder()
                    .userId(summary.getUserId())
                    .weekStart(summary.getWeekStart())
                    .weekEnd(summary.getWeekEnd())
                    .dailySummaries(summary.getDailySummaries())
                    .build();
            
            return ApiResult.success("주간 근무 상세를 성공적으로 조회했습니다.", detail);
        } catch (Exception e) {
            return ApiResult.failure("주간 근무 상세 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /** 출근 가능 시간 조회 */
    @GetMapping("/check-in-available-time")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ApiResult<Map<String, Object>> getCheckInAvailableTime(@RequestParam Long userId) {
        try {
            Map<String, Object> response = attendanceService.getCheckInAvailableTime(userId);
            return ApiResult.success("출근 가능 시간을 성공적으로 조회했습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("출근 가능 시간 조회에 실패했습니다: " + e.getMessage());
        }
    }
} 
