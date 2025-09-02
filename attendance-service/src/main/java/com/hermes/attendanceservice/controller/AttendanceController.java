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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Attendance", description = "출근/퇴근 관리 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "출근 체크인", description = "직원의 출근 시간을 기록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "출근 기록 성공",
            content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/check-in")
    public ApiResult<AttendanceResponse> checkIn(
            @Parameter(description = "출근 요청 정보") @RequestBody CheckInRequest request,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
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

    @Operation(summary = "퇴근 체크아웃", description = "직원의 퇴근 시간을 기록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "퇴근 기록 성공",
            content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/check-out")
    public ApiResult<AttendanceResponse> checkOut(
            @Parameter(description = "퇴근 요청 정보") @RequestBody CheckOutRequest request,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
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

    @Operation(summary = "출근 상태 기록", description = "직원의 출근 상태를 수동으로 기록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "출근 상태 기록 성공",
            content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/attendance-status")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ApiResult<AttendanceResponse> markAttendanceStatus(
            @Parameter(description = "직원 ID") @RequestParam Long userId,
            @Parameter(description = "날짜 (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "출근 상태") @RequestParam AttendanceStatus attendanceStatus,
            @Parameter(description = "자동 기록 여부") @RequestParam(defaultValue = "true") boolean autoRecorded,
            @Parameter(description = "출근 시간 (선택사항)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
            @Parameter(description = "퇴근 시간 (선택사항)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime) {
        try {
            AttendanceResponse response = attendanceService.markAttendanceStatus(userId, date, attendanceStatus, autoRecorded, checkInTime, checkOutTime);
            return ApiResult.success("출근 상태가 성공적으로 기록되었습니다.", response);
        } catch (Exception e) {
            return ApiResult.failure("출근 상태 기록에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "근무 상태 기록", description = "직원의 근무 상태를 수동으로 기록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 상태 기록 성공",
            content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/work-status")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ApiResult<AttendanceResponse> markWorkStatus(
            @Parameter(description = "직원 ID") @RequestParam Long userId,
            @Parameter(description = "날짜 (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "근무 상태") @RequestParam WorkStatus workStatus,
            @Parameter(description = "자동 기록 여부") @RequestParam(defaultValue = "true") boolean autoRecorded,
            @Parameter(description = "출근 시간 (선택사항)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
            @Parameter(description = "퇴근 시간 (선택사항)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime) {
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
