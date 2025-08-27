package com.hermes.workscheduleservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.workscheduleservice.dto.AdjustWorkTimeRequestDto;
import com.hermes.workscheduleservice.dto.CreateScheduleRequestDto;
import com.hermes.workscheduleservice.dto.ScheduleResponseDto;
import com.hermes.workscheduleservice.dto.UpdateScheduleRequestDto;
import com.hermes.workscheduleservice.dto.UserWorkPolicyDto;
import com.hermes.workscheduleservice.dto.WorkPolicyDto;
import com.hermes.workscheduleservice.entity.WorkTimeAdjustment;
import com.hermes.workscheduleservice.service.WorkScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResult<UserWorkPolicyDto>> getUserWorkPolicy(@PathVariable Long userId) {
        try {
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
    
    /**
     * 사용자 ID를 통해 근무 정책 정보만 조회
     */
    @GetMapping("/users/{userId}/work-policy-only")
    public ResponseEntity<ApiResult<WorkPolicyDto>> getUserWorkPolicyOnly(@PathVariable Long userId) {
        try {
            WorkPolicyDto result = workScheduleService.getUserWorkPolicyOnly(userId);
            
            if (result == null) {
                return ResponseEntity.ok(ApiResult.failure("사용자의 근무 정책을 찾을 수 없습니다."));
            }
            
            return ResponseEntity.ok(ApiResult.success("근무 정책 조회 성공", result));
        } catch (Exception e) {
            log.error("Error in getUserWorkPolicyOnly for userId: {}", userId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 정책 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 새로운 스케줄 생성
     */
    @PostMapping("/schedules")
    public ResponseEntity<ApiResult<ScheduleResponseDto>> createSchedule(@Valid @RequestBody CreateScheduleRequestDto requestDto) {
        try {
            log.info("Creating schedule for userId: {}", requestDto.getUserId());
            ScheduleResponseDto result = workScheduleService.createSchedule(requestDto);
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
     * 스케줄 수정
     */
    @PutMapping("/users/{userId}/schedules/{scheduleId}")
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
     * 사용자별 스케줄 조회
     */
    @GetMapping("/users/{userId}/schedules")
    public ResponseEntity<ApiResult<List<ScheduleResponseDto>>> getUserSchedules(@PathVariable Long userId) {
        try {
            List<ScheduleResponseDto> schedules = workScheduleService.getUserSchedules(userId);
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
    public ResponseEntity<ApiResult<WorkTimeAdjustment>> createWorkTimeAdjustment(@Valid @RequestBody AdjustWorkTimeRequestDto requestDto) {
        try {
            log.info("Creating work time adjustment for userId: {}", requestDto.getUserId());
            WorkTimeAdjustment result = workScheduleService.createWorkTimeAdjustment(requestDto);
            return ResponseEntity.ok(ApiResult.success("근무 시간 조정 요청 생성 성공", result));
        } catch (Exception e) {
            log.error("Error creating work time adjustment for userId: {}", requestDto.getUserId(), e);
            return ResponseEntity.ok(ApiResult.failure("근무 시간 조정 요청 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 시간 조정 승인/거절
     */
    @PutMapping("/work-time-adjustments/{adjustmentId}/process")
    public ResponseEntity<ApiResult<WorkTimeAdjustment>> processWorkTimeAdjustment(
            @PathVariable Long adjustmentId,
            @RequestParam String approverId,
            @RequestParam boolean isApproved,
            @RequestParam(required = false) String comment) {
        try {
            log.info("Processing work time adjustment: {} by approver: {}", adjustmentId, approverId);
            WorkTimeAdjustment result = workScheduleService.processWorkTimeAdjustment(adjustmentId, approverId, isApproved, comment);
            String message = isApproved ? "근무 시간 조정 승인 성공" : "근무 시간 조정 거절 성공";
            return ResponseEntity.ok(ApiResult.success(message, result));
        } catch (Exception e) {
            log.error("Error processing work time adjustment: {}", adjustmentId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 시간 조정 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 시간 조정 삭제 (취소)
     */
    @DeleteMapping("/users/{userId}/work-time-adjustments/{adjustmentId}")
    public ResponseEntity<ApiResult<Void>> deleteWorkTimeAdjustment(
            @PathVariable Long userId,
            @PathVariable Long adjustmentId) {
        try {
            log.info("Deleting work time adjustment: {} for userId: {}", adjustmentId, userId);
            workScheduleService.deleteWorkTimeAdjustment(userId, adjustmentId);
            return ResponseEntity.ok(ApiResult.success("근무 시간 조정 삭제 성공"));
        } catch (Exception e) {
            log.error("Error deleting work time adjustment: {} for userId: {}", adjustmentId, userId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 시간 조정 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 사용자별 근무 시간 조정 조회
     */
    @GetMapping("/users/{userId}/work-time-adjustments")
    public ResponseEntity<ApiResult<List<WorkTimeAdjustment>>> getUserWorkTimeAdjustments(@PathVariable Long userId) {
        try {
            List<WorkTimeAdjustment> adjustments = workScheduleService.getUserWorkTimeAdjustments(userId);
            return ResponseEntity.ok(ApiResult.success("근무 시간 조정 조회 성공", adjustments));
        } catch (Exception e) {
            log.error("Error fetching work time adjustments for userId: {}", userId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 시간 조정 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 시간 조정 상세 조회
     */
    @GetMapping("/users/{userId}/work-time-adjustments/{adjustmentId}")
    public ResponseEntity<ApiResult<WorkTimeAdjustment>> getWorkTimeAdjustmentById(
            @PathVariable Long userId,
            @PathVariable Long adjustmentId) {
        try {
            WorkTimeAdjustment adjustment = workScheduleService.getWorkTimeAdjustmentById(userId, adjustmentId);
            if (adjustment == null) {
                return ResponseEntity.ok(ApiResult.failure("근무 시간 조정을 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(ApiResult.success("근무 시간 조정 상세 조회 성공", adjustment));
        } catch (Exception e) {
            log.error("Error fetching work time adjustment: {} for userId: {}", adjustmentId, userId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 시간 조정 상세 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 관리자용 근무 시간 조정 상세 조회
     */
    @GetMapping("/work-time-adjustments/{adjustmentId}")
    public ResponseEntity<ApiResult<WorkTimeAdjustment>> getWorkTimeAdjustmentByIdForAdmin(@PathVariable Long adjustmentId) {
        try {
            WorkTimeAdjustment adjustment = workScheduleService.getWorkTimeAdjustmentByIdForAdmin(adjustmentId);
            if (adjustment == null) {
                return ResponseEntity.ok(ApiResult.failure("근무 시간 조정을 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(ApiResult.success("근무 시간 조정 상세 조회 성공", adjustment));
        } catch (Exception e) {
            log.error("Error fetching work time adjustment: {}", adjustmentId, e);
            return ResponseEntity.ok(ApiResult.failure("근무 시간 조정 상세 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 승인 대기 중인 근무 시간 조정 조회
     */
    @GetMapping("/work-time-adjustments/pending")
    public ResponseEntity<ApiResult<List<WorkTimeAdjustment>>> getPendingWorkTimeAdjustments() {
        try {
            List<WorkTimeAdjustment> adjustments = workScheduleService.getPendingWorkTimeAdjustments();
            return ResponseEntity.ok(ApiResult.success("승인 대기 중인 근무 시간 조정 조회 성공", adjustments));
        } catch (Exception e) {
            log.error("Error fetching pending work time adjustments", e);
            return ResponseEntity.ok(ApiResult.failure("승인 대기 중인 근무 시간 조정 조회 중 오류가 발생했습니다."));
        }
    }
} 