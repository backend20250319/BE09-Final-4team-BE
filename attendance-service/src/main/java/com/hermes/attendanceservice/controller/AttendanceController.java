package com.hermes.attendanceservice.controller;

import com.hermes.attendanceservice.common.ApiResponse;
import com.hermes.attendanceservice.dto.AttendanceResponse;
import com.hermes.attendanceservice.dto.WeeklyWorkSummary;
import com.hermes.attendanceservice.entity.WorkStatus;
import com.hermes.attendanceservice.service.AttendanceService;
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
    public ApiResponse<AttendanceResponse> checkIn(@RequestParam Long userId,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                      LocalDateTime checkInTime) {
        try {
            AttendanceResponse response = attendanceService.checkIn(userId, checkInTime);
            return ApiResponse.success("출근 기록이 성공적으로 등록되었습니다.", response);
        } catch (Exception e) {
            return ApiResponse.failure("출근 기록 등록에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/check-out")
    public ApiResponse<AttendanceResponse> checkOut(@RequestParam Long userId,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                       LocalDateTime checkOutTime) {
        try {
            AttendanceResponse response = attendanceService.checkOut(userId, checkOutTime);
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

    /** 이번 주(일~토) 요약 */
    @GetMapping("/weekly/this")
    public ApiResponse<WeeklyWorkSummary> getThisWeek(@RequestParam Long userId) {
        try {
            WeeklyWorkSummary summary = attendanceService.getThisWeekSummary(userId);
            return ApiResponse.success("이번 주 근무 요약을 성공적으로 조회했습니다.", summary);
        } catch (Exception e) {
            return ApiResponse.failure("이번 주 근무 요약 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /** 임의 주(weekStart가 일요일이 아니어도 자동 보정) */
    @GetMapping("/weekly")
    public ApiResponse<WeeklyWorkSummary> getWeek(@RequestParam Long userId,
                                     @RequestParam
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                     LocalDate weekStart) {
        try {
            WeeklyWorkSummary summary = attendanceService.getWeekSummary(userId, weekStart);
            return ApiResponse.success("주간 근무 요약을 성공적으로 조회했습니다.", summary);
        } catch (Exception e) {
            return ApiResponse.failure("주간 근무 요약 조회에 실패했습니다: " + e.getMessage());
        }
    }
}
