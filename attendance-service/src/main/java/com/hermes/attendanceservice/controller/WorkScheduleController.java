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
public class WorkScheduleController {
    
    private final WorkScheduleService workScheduleService;
    
    /**
     * 사용자 ID를 통해 해당 사용자의 근무 정책 정보를 조회
     */
    @GetMapping("/users/{userId}/work-policy")
    public ResponseEntity<ApiResult<UserWorkPolicyDto>> getUserWorkPolicy(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인 또는 관리자만 조회 가능
            if (!user.getUserId().equals(userId) && !user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.ok(ApiResult.failure("권한이 없습니다."));
            }
            
            // Authorization 헤더는 null로 전달 (User Service에서 직접 처리)
            UserWorkPolicyDto result = workScheduleService.getUserWorkPolicy(userId, null);
            
            if (result == null) {
                return ResponseEntity.ok(ApiResult.failure("사용자의 근무 정책을 찾을 수 없습니다."));
            }
            
            return ResponseEntity.ok(ApiResult.success("근무 정책 조회 성공", result));
        } catch (Exception e) {
            log.error("Error in getUserWorkPolicy for userId: {}", userId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 정책 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 새로운 스케줄 생성
     */
    @PostMapping("/schedules")
    public ResponseEntity<ApiResult<ScheduleResponseDto>> createSchedule(
            @Valid @RequestBody CreateScheduleRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인 또는 관리자만 생성 가능
            if (!user.getUserId().equals(requestDto.getUserId()) && !user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.ok(ApiResult.failure("권한이 없습니다."));
            }
            
            log.info("Creating schedule for userId: {}", requestDto.getUserId());
            ScheduleResponseDto result = workScheduleService.createSchedule(requestDto, null);
            return ResponseEntity.ok(ApiResult.success("스케줄 생성 성공", result));
        } catch (Exception e) {
            log.error("Error creating schedule for userId: {}", requestDto.getUserId(), e);
            return ResponseEntity.ok(ApiResult.failure("스케줄 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * Work Policy 기반 고정 스케줄 생성
     */
    @PostMapping("/users/{userId}/fixed-schedules")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResult<List<ScheduleResponseDto>>> createFixedSchedules(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
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
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
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
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
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
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
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
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
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
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
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
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
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
            if (!user.getUserId().equals(requestDto.getUserId())) {
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
    @GetMapping("/colleagues/{colleagueId}/schedules")
    public ResponseEntity<ApiResult<ColleagueScheduleResponseDto>> getColleagueSchedule(
            @PathVariable Long colleagueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal user) {
        try {
            log.info("Fetching colleague schedule for colleagueId: {} from {} to {}", colleagueId, startDate, endDate);
            
            // 본인 또는 관리자만 조회 가능
            if (!user.getUserId().equals(colleagueId) && !user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.ok(ApiResult.failure("권한이 없습니다."));
            }
            
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