package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.workschedule.AdjustWorkTimeRequestDto;
import com.hermes.attendanceservice.dto.workschedule.ColleagueScheduleResponseDto;
import com.hermes.attendanceservice.dto.workschedule.CreateScheduleRequestDto;
import com.hermes.attendanceservice.dto.workschedule.ScheduleResponseDto;
import com.hermes.attendanceservice.dto.workschedule.UpdateScheduleRequestDto;
import com.hermes.attendanceservice.dto.workschedule.UserWorkPolicyDto;
import com.hermes.attendanceservice.entity.workschedule.WorkTimeAdjustment;
import com.hermes.attendanceservice.service.workschedule.WorkScheduleService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work-schedule")
@RequiredArgsConstructor
@Tag(name = "Work Schedule", description = "근무 스케줄 관리 API")
public class WorkScheduleController {
    
    private final WorkScheduleService workScheduleService;
    
    @Operation(summary = "사용자 근무 정책 조회", description = "특정 사용자의 근무 정책 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "근무 정책 조회 성공",
            content = @Content(schema = @Schema(implementation = UserWorkPolicyDto.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "근무 정책을 찾을 수 없음")
    })
    @GetMapping("/users/{userId}/work-policy")
    public ResponseEntity<ApiResult<UserWorkPolicyDto>> getUserWorkPolicy(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인 또는 관리자만 조회 가능
            if (!user.getId().equals(userId) && !user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.ok(ApiResult.failure("권한이 없습니다."));
            }
            
            // Authorization 헤더는 null로 전달 (User Service에서 직접 처리)
            UserWorkPolicyDto result = workScheduleService.getUserWorkPolicy(userId);
            
            if (result == null) {
                return ResponseEntity.ok(ApiResult.failure("사용자의 근무 정책을 찾을 수 없습니다."));
            }
            
            return ResponseEntity.ok(ApiResult.success("근무 정책 조회 성공", result));
        } catch (Exception e) {
            log.error("Error in getUserWorkPolicy for userId: {}", userId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 정책 조회 중 오류가 발생했습니다."));
        }
    }
    
    @Operation(summary = "스케줄 생성", description = "새로운 근무 스케줄을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "스케줄 생성 성공",
            content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/schedules")
    public ResponseEntity<ApiResult<ScheduleResponseDto>> createSchedule(
            @Parameter(description = "스케줄 생성 정보") @Valid @RequestBody CreateScheduleRequestDto requestDto,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인 또는 관리자만 생성 가능
            if (!user.getId().equals(requestDto.getUserId()) && !user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.ok(ApiResult.failure("권한이 없습니다."));
            }
            
            log.info("Creating schedule for userId: {}", requestDto.getUserId());
            ScheduleResponseDto result = workScheduleService.createSchedule(requestDto, null);
            return ResponseEntity.ok(ApiResult.success("스케줄 생성 성공", result));
        } catch (Exception e) {
            log.error("Error creating schedule for userId: {}", requestDto.getUserId(), e);
            return ResponseEntity.ok(ApiResult.failure("스케줄 생성 실패: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "고정 스케줄 생성", description = "Work Policy 기반으로 고정 스케줄을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "고정 스케줄 생성 성공",
            content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/users/{userId}/fixed-schedules")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResult<List<ScheduleResponseDto>>> createFixedSchedules(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "시작 날짜 (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("Creating fixed schedules for userId: {} from {} to {}", userId, startDate, endDate);
            List<ScheduleResponseDto> results = workScheduleService.createFixedSchedulesFromWorkPolicy(userId, startDate, endDate);
            return ResponseEntity.ok(ApiResult.success("고정 스케줄 생성 성공", results));
        } catch (Exception e) {
            log.error("Error creating fixed schedules for userId: {} from {} to {}", userId, startDate, endDate, e);
            return ResponseEntity.ok(ApiResult.failure("고정 스케줄 생성 중 오류가 발생했습니다."));
        }
    }

    /**
     * WorkPolicy 정보를 Workschedule에 반영하여 저장
     * 근무시간, 휴게시간, 출근시간, 퇴근시간, 코어시간, 시차 근무 출근 가능 시간 등을 스케줄로 생성
     */
    @PostMapping("/users/{userId}/apply-work-policy")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResult<List<ScheduleResponseDto>>> applyWorkPolicyToSchedule(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("Applying work policy to schedule for userId: {} from {} to {}", userId, startDate, endDate);
            List<ScheduleResponseDto> results = workScheduleService.applyWorkPolicyToSchedule(userId, null, startDate, endDate);
            return ResponseEntity.ok(ApiResult.success("근무 정책이 스케줄에 성공적으로 반영되었습니다.", results));
        } catch (Exception e) {
            log.error("Error applying work policy to schedule for userId: {} from {} to {}", userId, startDate, endDate, e);
            return ResponseEntity.ok(ApiResult.failure("근무 정책을 스케줄에 반영하는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 스케줄 수정
     */
    @PutMapping("/users/{userId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResult<ScheduleResponseDto>> updateSchedule(
            @PathVariable Long userId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateScheduleRequestDto requestDto) {
        try {
            log.info("Updating schedule: {} for userId: {}", scheduleId, userId);
            ScheduleResponseDto result = workScheduleService.updateSchedule(userId, scheduleId, requestDto);
            return ResponseEntity.ok(ApiResult.success("스케줄 수정 성공", result));
        } catch (Exception e) {
            log.error("Error updating schedule: {} for userId: {}", scheduleId, userId, e);
            return ResponseEntity.ok(ApiResult.failure("스케줄 수정 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 스케줄 삭제
     */
    @DeleteMapping("/users/{userId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResult<Void>> deleteSchedule(
            @PathVariable Long userId,
            @PathVariable Long scheduleId) {
        try {
            log.info("Deleting schedule: {} for userId: {}", scheduleId, userId);
            workScheduleService.deleteSchedule(userId, scheduleId);
            return ResponseEntity.ok(ApiResult.success("스케줄 삭제 성공"));
        } catch (Exception e) {
            log.error("Error deleting schedule: {} for userId: {}", scheduleId, userId, e);
            return ResponseEntity.ok(ApiResult.failure("스케줄 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 사용자별 스케줄 조회 (WorkPolicy 정보 포함)
     */
    @GetMapping("/users/{userId}/schedules")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResult<List<ScheduleResponseDto>>> getUserSchedules(
            @PathVariable Long userId) {
        try {
            List<ScheduleResponseDto> schedules = workScheduleService.getUserSchedules(userId, null);
            return ResponseEntity.ok(ApiResult.success("스케줄 조회 성공", schedules));
        } catch (Exception e) {
            log.error("Error fetching schedules for userId: {}", userId, e);
            return ResponseEntity.ok(ApiResult.failure("스케줄 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 사용자별 특정 기간 스케줄 조회
     */
    @GetMapping("/users/{userId}/schedules/range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResult<List<ScheduleResponseDto>>> getUserSchedulesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<ScheduleResponseDto> schedules = workScheduleService.getUserSchedulesByDateRange(userId, startDate, endDate);
            return ResponseEntity.ok(ApiResult.success("기간별 스케줄 조회 성공", schedules));
        } catch (Exception e) {
            log.error("Error fetching schedules for userId: {} between {} and {}", userId, startDate, endDate, e);
            return ResponseEntity.ok(ApiResult.failure("기간별 스케줄 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 스케줄 상세 조회
     */
    @GetMapping("/users/{userId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResult<ScheduleResponseDto>> getScheduleById(
            @PathVariable Long userId,
            @PathVariable Long scheduleId) {
        try {
            ScheduleResponseDto schedule = workScheduleService.getScheduleById(userId, scheduleId);
            if (schedule == null) {
                return ResponseEntity.ok(ApiResult.failure("스케줄을 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(ApiResult.success("스케줄 상세 조회 성공", schedule));
        } catch (Exception e) {
            log.error("Error fetching schedule: {} for userId: {}", scheduleId, userId, e);
            return ResponseEntity.ok(ApiResult.failure("스케줄 상세 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 시간 조정 요청 생성
     */
    @PostMapping("/work-time-adjustments")
    public ResponseEntity<ApiResult<WorkTimeAdjustment>> createWorkTimeAdjustment(
            @Valid @RequestBody AdjustWorkTimeRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인만 조정 요청 가능
            if (!user.getId().equals(requestDto.getUserId())) {
                return ResponseEntity.ok(ApiResult.failure("권한이 없습니다."));
            }
            
            log.info("Creating work time adjustment for userId: {}", requestDto.getUserId());
            WorkTimeAdjustment result = workScheduleService.createWorkTimeAdjustment(requestDto);
            return ResponseEntity.ok(ApiResult.success("근무 시간 조정 요청 생성 성공", result));
        } catch (Exception e) {
            log.error("Error creating work time adjustment for userId: {}", requestDto.getUserId(), e);
            return ResponseEntity.ok(ApiResult.failure("근무 시간 조정 요청 생성 중 오류가 발생했습니다."));
        }
    }

    /**
     * 동료 근무표 조회
     */
    @Operation(summary = "동료 근무표 조회", description = "특정 동료의 근무 스케줄을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "동료 근무표 조회 성공",
            content = @Content(schema = @Schema(implementation = ColleagueScheduleResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "동료 근무표를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/colleagues/{colleagueId}/schedules")
    public ResponseEntity<ApiResult<ColleagueScheduleResponseDto>> getColleagueSchedule(
            @Parameter(description = "동료 사용자 ID") @PathVariable Long colleagueId,
            @Parameter(description = "시작 날짜 (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            log.info("Fetching colleague schedule for colleagueId: {} from {} to {} by user: {}", colleagueId, startDate, endDate, user.getId());
            
            ColleagueScheduleResponseDto result = workScheduleService.getColleagueSchedule(colleagueId, startDate, endDate);
            
            if (result == null) {
                return ResponseEntity.ok(ApiResult.failure("동료의 근무표를 찾을 수 없습니다."));
            }
            
            return ResponseEntity.ok(ApiResult.success("동료 근무표 조회 성공", result));
        } catch (Exception e) {
            log.error("Error fetching colleague schedule for colleagueId: {} from {} to {}", colleagueId, startDate, endDate, e);
            return ResponseEntity.ok(ApiResult.failure("동료 근무표 조회 중 오류가 발생했습니다."));
        }
    }
} 