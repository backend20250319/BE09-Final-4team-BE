package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.attendance.AttendanceResponse;
import com.hermes.attendanceservice.dto.attendance.WeeklyWorkSummary;
import com.hermes.attendanceservice.dto.attendance.WeeklyWorkDetail;
import com.hermes.attendanceservice.dto.attendance.CheckInRequest;
import com.hermes.attendanceservice.dto.attendance.CheckOutRequest;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;
import com.hermes.attendanceservice.service.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ApiResult<AttendanceResponse> checkIn(@RequestBody CheckInRequest request) {
        try {
            AttendanceResponse response = attendanceService.checkIn(request.getUserId(), request.getCheckIn());
            return ApiResult.success("출근 기록이 성공적으로 등록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("출근 기록 등록에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/check-out")
    public ApiResult<AttendanceResponse> checkOut(@RequestBody CheckOutRequest request) {
        try {
            AttendanceResponse response = attendanceService.checkOut(request.getUserId(), request.getCheckOut());
            return ApiResult.success("퇴근 기록이 성공적으로 등록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("퇴근 기록 등록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 휴가/출장/재택/택시 근무 상태 기록 */
    @PostMapping("/status")
    public ApiResult<AttendanceResponse> markStatus(@RequestParam Long userId,
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
            return ApiResult.success("근무 상태가 성공적으로 기록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("근무 상태 기록에 실패했습니다: " + e.getMessage());
        }
    }

    /** 이번 주 근무 상세 */
    @GetMapping("/weekly/this")
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
    public ApiResult<WeeklyWorkDetail> getWeek(@RequestParam Long userId,
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
            
            return ApiResult.success("주간 근무 상세를 성공적으로 조회했습니다.", detail);
        } catch (Exception e) {
            return ApiResult.failure("주간 근무 상세 조회에 실패했습니다: " + e.getMessage());
        }
    }
} 
