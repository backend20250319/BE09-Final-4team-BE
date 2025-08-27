package com.hermes.workscheduleservice.service;

import com.hermes.workscheduleservice.client.AttendanceServiceClient;
import com.hermes.workscheduleservice.client.UserServiceClient;
import com.hermes.workscheduleservice.dto.AdjustWorkTimeRequestDto;
import com.hermes.workscheduleservice.dto.CreateScheduleRequestDto;
import com.hermes.workscheduleservice.dto.ScheduleResponseDto;
import com.hermes.workscheduleservice.dto.UpdateScheduleRequestDto;
import com.hermes.workscheduleservice.dto.UserWorkPolicyDto;
import com.hermes.workscheduleservice.dto.WorkPolicyDto;
import com.hermes.workscheduleservice.entity.Schedule;
import com.hermes.workscheduleservice.entity.WorkTimeAdjustment;
import com.hermes.workscheduleservice.repository.ScheduleRepository;
import com.hermes.workscheduleservice.repository.WorkTimeAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkScheduleService {
    
    private final UserServiceClient userServiceClient;
    private final AttendanceServiceClient attendanceServiceClient;
    private final ScheduleRepository scheduleRepository;
    private final WorkTimeAdjustmentRepository workTimeAdjustmentRepository;
    
    /**
     * 사용자 ID를 통해 해당 사용자의 근무 정책 정보를 조회
     */
    public UserWorkPolicyDto getUserWorkPolicy(Long userId) {
        try {
            // 1. User Service에서 사용자의 workPolicyId 조회
            Map<String, Object> userResponse = userServiceClient.getUserById(userId);
            
            if (userResponse == null) {
                log.warn("User not found with id: {}", userId);
                return null;
            }
            
            // 2. 사용자의 workPolicyId가 있는지 확인
            Long workPolicyId = userResponse.get("workPolicyId") != null ? 
                Long.valueOf(userResponse.get("workPolicyId").toString()) : null;
            
            if (workPolicyId == null) {
                log.warn("User {} has no work policy assigned", userId);
                return UserWorkPolicyDto.builder()
                        .workPolicyId(null)
                        .workPolicy(null)
                        .build();
            }
            
            // 3. Attendance Service에서 근무 정책 정보 조회
            WorkPolicyDto workPolicy = attendanceServiceClient.getWorkPolicyById(workPolicyId);
            
            return UserWorkPolicyDto.builder()
                    .workPolicyId(workPolicyId)
                    .workPolicy(workPolicy)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error fetching user work policy for userId: {}", userId, e);
            throw new RuntimeException("Failed to fetch user work policy", e);
        }
    }
    
    /**
     * 사용자 ID를 통해 근무 정책 정보만 조회
     */
    public WorkPolicyDto getUserWorkPolicyOnly(Long userId) {
        UserWorkPolicyDto userWorkPolicy = getUserWorkPolicy(userId);
        return userWorkPolicy != null ? userWorkPolicy.getWorkPolicy() : null;
    }
    
    /**
     * 새로운 스케줄 생성
     */
    @Transactional
    public ScheduleResponseDto createSchedule(CreateScheduleRequestDto requestDto) {
        try {
            // 1. 사용자 존재 여부 확인
            Map<String, Object> userResponse = userServiceClient.getUserById(requestDto.getUserId());
            if (userResponse == null) {
                throw new RuntimeException("User not found with id: " + requestDto.getUserId());
            }
            
            // 2. 스케줄 중복 확인
            boolean hasConflict = scheduleRepository.existsConflictingSchedule(
                requestDto.getUserId(),
                null, // 새 스케줄이므로 ID는 null
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                requestDto.getStartTime().toString(),
                requestDto.getEndTime().toString()
            );
            
            if (hasConflict) {
                throw new RuntimeException("Schedule conflict detected for the specified time period");
            }
            
            // 3. 스케줄 생성
            Schedule schedule = Schedule.builder()
                    .userId(requestDto.getUserId())
                    .title(requestDto.getTitle())
                    .description(requestDto.getDescription())
                    .startDate(requestDto.getStartDate())
                    .endDate(requestDto.getEndDate())
                    .startTime(requestDto.getStartTime())
                    .endTime(requestDto.getEndTime())
                    .scheduleType(requestDto.getScheduleType())
                    .color(requestDto.getColor())
                    .isAllDay(requestDto.getIsAllDay())
                    .isRecurring(requestDto.getIsRecurring())
                    .recurrencePattern(requestDto.getRecurrencePattern())
                    .recurrenceInterval(requestDto.getRecurrenceInterval())
                    .recurrenceDays(requestDto.getRecurrenceDays())
                    .recurrenceEndDate(requestDto.getRecurrenceEndDate())
                    .workPolicyId(requestDto.getWorkPolicyId())
                    .priority(requestDto.getPriority())
                    .location(requestDto.getLocation())
                    .attendees(requestDto.getAttendees())
                    .notes(requestDto.getNotes())
                    .status("ACTIVE")
                    .build();
            
            Schedule savedSchedule = scheduleRepository.save(schedule);
            
            log.info("Schedule created successfully: {}", savedSchedule.getId());
            
            return convertToResponseDto(savedSchedule);
            
        } catch (Exception e) {
            log.error("Error creating schedule for userId: {}", requestDto.getUserId(), e);
            throw new RuntimeException("Failed to create schedule", e);
        }
    }
    
    /**
     * 스케줄 수정
     */
    @Transactional
    public ScheduleResponseDto updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequestDto requestDto) {
        try {
            // 1. 스케줄 존재 여부 및 소유권 확인
            Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found or access denied"));
            
            // 2. 고정 스케줄 수정 방지 검증
            validateScheduleEditability(schedule);
            
            // 3. 스케줄 중복 확인 (자신 제외)
            boolean hasConflict = scheduleRepository.existsConflictingSchedule(
                userId,
                scheduleId,
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                requestDto.getStartTime().toString(),
                requestDto.getEndTime().toString()
            );
            
            if (hasConflict) {
                throw new RuntimeException("Schedule conflict detected for the specified time period");
            }
            
            // 4. 스케줄 업데이트
            schedule = Schedule.builder()
                    .id(scheduleId)
                    .userId(userId)
                    .title(requestDto.getTitle())
                    .description(requestDto.getDescription())
                    .startDate(requestDto.getStartDate())
                    .endDate(requestDto.getEndDate())
                    .startTime(requestDto.getStartTime())
                    .endTime(requestDto.getEndTime())
                    .scheduleType(requestDto.getScheduleType())
                    .color(requestDto.getColor())
                    .isAllDay(requestDto.getIsAllDay())
                    .isRecurring(requestDto.getIsRecurring())
                    .recurrencePattern(requestDto.getRecurrencePattern())
                    .recurrenceInterval(requestDto.getRecurrenceInterval())
                    .recurrenceDays(requestDto.getRecurrenceDays())
                    .recurrenceEndDate(requestDto.getRecurrenceEndDate())
                    .workPolicyId(requestDto.getWorkPolicyId())
                    .priority(requestDto.getPriority())
                    .location(requestDto.getLocation())
                    .attendees(requestDto.getAttendees())
                    .notes(requestDto.getNotes())
                    .status(schedule.getStatus())
                    .createdAt(schedule.getCreatedAt())
                    .isFixed(schedule.getIsFixed())
                    .isEditable(schedule.getIsEditable())
                    .fixedReason(schedule.getFixedReason())
                    .build();
            
            Schedule updatedSchedule = scheduleRepository.save(schedule);
            
            log.info("Schedule updated successfully: {}", updatedSchedule.getId());
            
            return convertToResponseDto(updatedSchedule);
            
        } catch (Exception e) {
            log.error("Error updating schedule: {} for userId: {}", scheduleId, userId, e);
            throw new RuntimeException("Failed to update schedule", e);
        }
    }
    
    /**
     * 스케줄 삭제
     */
    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        try {
            Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found or access denied"));
            
            // 고정 스케줄 삭제 방지 검증
            validateScheduleEditability(schedule);
            
            schedule.cancel(); // 상태를 CANCELLED로 변경
            scheduleRepository.save(schedule);
            
            log.info("Schedule deleted successfully: {}", scheduleId);
            
        } catch (Exception e) {
            log.error("Error deleting schedule: {} for userId: {}", scheduleId, userId, e);
            throw new RuntimeException("Failed to delete schedule", e);
        }
    }
    
    /**
     * 근무 시간 조정 요청 생성
     */
    @Transactional
    public WorkTimeAdjustment createWorkTimeAdjustment(AdjustWorkTimeRequestDto requestDto) {
        try {
            // 1. 사용자 존재 여부 확인
            Map<String, Object> userResponse = userServiceClient.getUserById(requestDto.getUserId());
            if (userResponse == null) {
                throw new RuntimeException("User not found with id: " + requestDto.getUserId());
            }
            
            // 2. 같은 날짜에 이미 조정 요청이 있는지 확인
            workTimeAdjustmentRepository.findByUserIdAndAdjustDate(requestDto.getUserId(), requestDto.getAdjustDate())
                    .ifPresent(existing -> {
                        throw new RuntimeException("Work time adjustment already exists for the specified date");
                    });
            
            // 3. 근무 시간 조정 요청 생성
            WorkTimeAdjustment adjustment = WorkTimeAdjustment.builder()
                    .userId(requestDto.getUserId())
                    .adjustDate(requestDto.getAdjustDate())
                    .adjustType(requestDto.getAdjustType())
                    .startTime(requestDto.getStartTime())
                    .endTime(requestDto.getEndTime())
                    .reason(requestDto.getReason())
                    .description(requestDto.getDescription())
                    .isApproved(requestDto.getIsApproved())
                    .approverId(requestDto.getApproverId())
                    .approverComment(requestDto.getApproverComment())
                    .status("PENDING")
                    .build();
            
            WorkTimeAdjustment savedAdjustment = workTimeAdjustmentRepository.save(adjustment);
            
            log.info("Work time adjustment created successfully: {}", savedAdjustment.getId());
            
            return savedAdjustment;
            
        } catch (Exception e) {
            log.error("Error creating work time adjustment for userId: {}", requestDto.getUserId(), e);
            throw new RuntimeException("Failed to create work time adjustment", e);
        }
    }
    
    /**
     * 근무 시간 조정 승인/거절
     */
    @Transactional
    public WorkTimeAdjustment processWorkTimeAdjustment(Long adjustmentId, String approverId, boolean isApproved, String comment) {
        try {
            WorkTimeAdjustment adjustment = workTimeAdjustmentRepository.findById(adjustmentId)
                    .orElseThrow(() -> new RuntimeException("Work time adjustment not found"));
            
            if (isApproved) {
                adjustment.approve(approverId, comment);
            } else {
                adjustment.reject(approverId, comment);
            }
            
            WorkTimeAdjustment updatedAdjustment = workTimeAdjustmentRepository.save(adjustment);
            
            log.info("Work time adjustment {} successfully: {}", isApproved ? "approved" : "rejected", adjustmentId);
            
            return updatedAdjustment;
            
        } catch (Exception e) {
            log.error("Error processing work time adjustment: {}", adjustmentId, e);
            throw new RuntimeException("Failed to process work time adjustment", e);
        }
    }
    
    /**
     * 근무 시간 조정 삭제 (취소)
     */
    @Transactional
    public void deleteWorkTimeAdjustment(Long userId, Long adjustmentId) {
        try {
            // 1. 근무 시간 조정 존재 여부 및 소유권 확인
            WorkTimeAdjustment adjustment = workTimeAdjustmentRepository.findById(adjustmentId)
                    .orElseThrow(() -> new RuntimeException("Work time adjustment not found"));
            
            if (!adjustment.getUserId().equals(userId)) {
                throw new RuntimeException("Access denied: You can only delete your own work time adjustments");
            }
            
            // 2. 이미 승인된 조정은 삭제 불가
            if ("APPROVED".equals(adjustment.getStatus())) {
                throw new RuntimeException("Cannot delete approved work time adjustment");
            }
            
            // 3. 조정 상태를 CANCELLED로 변경
            adjustment.cancel();
            workTimeAdjustmentRepository.save(adjustment);
            
            log.info("Work time adjustment deleted successfully: {}", adjustmentId);
            
        } catch (Exception e) {
            log.error("Error deleting work time adjustment: {} for userId: {}", adjustmentId, userId, e);
            throw new RuntimeException("Failed to delete work time adjustment", e);
        }
    }
    
    /**
     * 근무 시간 조정 상세 조회
     */
    public WorkTimeAdjustment getWorkTimeAdjustmentById(Long userId, Long adjustmentId) {
        try {
            WorkTimeAdjustment adjustment = workTimeAdjustmentRepository.findById(adjustmentId)
                    .orElseThrow(() -> new RuntimeException("Work time adjustment not found"));
            
            // 소유권 확인 (관리자가 아닌 경우)
            if (!adjustment.getUserId().equals(userId)) {
                throw new RuntimeException("Access denied: You can only view your own work time adjustments");
            }
            
            return adjustment;
        } catch (Exception e) {
            log.error("Error fetching work time adjustment: {} for userId: {}", adjustmentId, userId, e);
            throw new RuntimeException("Failed to fetch work time adjustment", e);
        }
    }
    
    /**
     * 관리자용 근무 시간 조정 상세 조회
     */
    public WorkTimeAdjustment getWorkTimeAdjustmentByIdForAdmin(Long adjustmentId) {
        try {
            return workTimeAdjustmentRepository.findById(adjustmentId)
                    .orElseThrow(() -> new RuntimeException("Work time adjustment not found"));
        } catch (Exception e) {
            log.error("Error fetching work time adjustment: {}", adjustmentId, e);
            throw new RuntimeException("Failed to fetch work time adjustment", e);
        }
    }
    
    /**
     * Work Policy 기반 고정 스케줄 생성
     */
    @Transactional
    public List<ScheduleResponseDto> createFixedSchedulesFromWorkPolicy(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            // 1. 사용자의 work policy 조회
            UserWorkPolicyDto userWorkPolicy = getUserWorkPolicy(userId);
            if (userWorkPolicy == null || userWorkPolicy.getWorkPolicy() == null) {
                throw new RuntimeException("User has no work policy assigned");
            }
            
            WorkPolicyDto workPolicy = userWorkPolicy.getWorkPolicy();
            
            // 2. 기존 고정 스케줄 삭제 (해당 기간)
            deleteExistingFixedSchedules(userId, startDate, endDate);
            
            // 3. 근무일 스케줄 생성
            List<Schedule> workSchedules = createWorkDaySchedules(userId, workPolicy, startDate, endDate);
            
            // 4. 휴식 시간 스케줄 생성
            List<Schedule> breakSchedules = createBreakTimeSchedules(userId, workPolicy, startDate, endDate);
            
            // 5. 휴일 스케줄 생성
            List<Schedule> holidaySchedules = createHolidaySchedules(userId, workPolicy, startDate, endDate);
            
            // 6. 모든 스케줄 저장
            List<Schedule> allSchedules = new ArrayList<>();
            allSchedules.addAll(workSchedules);
            allSchedules.addAll(breakSchedules);
            allSchedules.addAll(holidaySchedules);
            
            List<Schedule> savedSchedules = scheduleRepository.saveAll(allSchedules);
            
            log.info("Created {} fixed schedules for userId: {} from {} to {}", 
                    savedSchedules.size(), userId, startDate, endDate);
            
            return savedSchedules.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error creating fixed schedules for userId: {} from {} to {}", userId, startDate, endDate, e);
            throw new RuntimeException("Failed to create fixed schedules", e);
        }
    }
    
    /**
     * 기존 고정 스케줄 삭제
     */
    private void deleteExistingFixedSchedules(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> existingFixedSchedules = scheduleRepository.findByUserIdAndDateRange(userId, "ACTIVE", startDate, endDate)
                .stream()
                .filter(schedule -> schedule.getIsFixed())
                .collect(Collectors.toList());
        
        for (Schedule schedule : existingFixedSchedules) {
            schedule.cancel();
        }
        scheduleRepository.saveAll(existingFixedSchedules);
        
        log.info("Deleted {} existing fixed schedules for userId: {}", existingFixedSchedules.size(), userId);
    }
    
    /**
     * 근무일 스케줄 생성
     */
    private List<Schedule> createWorkDaySchedules(Long userId, WorkPolicyDto workPolicy, LocalDate startDate, LocalDate endDate) {
        List<Schedule> workSchedules = new ArrayList<>();
        
        if (workPolicy.getWorkDays() == null || workPolicy.getWorkDays().isEmpty()) {
            return workSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays().contains(dayOfWeek)) {
                Schedule workSchedule = Schedule.builder()
                        .userId(userId)
                        .title("근무일")
                        .description("기본 근무 시간")
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getStartTime())
                        .endTime(workPolicy.getStartTime().plusHours(workPolicy.getWorkHours()).plusMinutes(workPolicy.getWorkMinutes()))
                        .scheduleType("WORK")
                        .color("#007bff")
                        .isAllDay(false)
                        .isRecurring(false)
                        .priority(1)
                        .isFixed(true)
                        .isEditable(false)
                        .fixedReason("WORK_POLICY")
                        .status("ACTIVE")
                        .build();
                
                workSchedules.add(workSchedule);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return workSchedules;
    }
    
    /**
     * 휴식 시간 스케줄 생성
     */
    private List<Schedule> createBreakTimeSchedules(Long userId, WorkPolicyDto workPolicy, LocalDate startDate, LocalDate endDate) {
        List<Schedule> breakSchedules = new ArrayList<>();
        
        if (workPolicy.getBreakStartTime() == null || workPolicy.getBreakMinutes() == null) {
            return breakSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                LocalTime breakEndTime = workPolicy.getBreakStartTime().plusMinutes(workPolicy.getBreakMinutes());
                
                Schedule breakSchedule = Schedule.builder()
                        .userId(userId)
                        .title("휴식 시간")
                        .description("점심 휴식")
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getBreakStartTime())
                        .endTime(breakEndTime)
                        .scheduleType("BREAK")
                        .color("#ffc107")
                        .isAllDay(false)
                        .isRecurring(false)
                        .priority(2)
                        .isFixed(true)
                        .isEditable(false)
                        .fixedReason("BREAK_TIME")
                        .status("ACTIVE")
                        .build();
                
                breakSchedules.add(breakSchedule);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return breakSchedules;
    }
    
    /**
     * 휴일 스케줄 생성
     */
    private List<Schedule> createHolidaySchedules(Long userId, WorkPolicyDto workPolicy, LocalDate startDate, LocalDate endDate) {
        List<Schedule> holidaySchedules = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            // 주말 휴일 체크
            boolean isWeekendHoliday = workPolicy.getHolidayDays() != null && 
                                     workPolicy.getHolidayDays().contains(dayOfWeek);
            
            // 공휴일 체크
            boolean isPublicHoliday = workPolicy.getHolidays() != null && 
                                    workPolicy.getHolidays().contains(currentDate.toString());
            
            if (isWeekendHoliday || isPublicHoliday) {
                String holidayTitle = isPublicHoliday ? "공휴일" : "주말 휴일";
                String holidayDescription = isPublicHoliday ? "법정 공휴일" : "주말 휴일";
                
                Schedule holidaySchedule = Schedule.builder()
                        .userId(userId)
                        .title(holidayTitle)
                        .description(holidayDescription)
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(LocalTime.of(0, 0))
                        .endTime(LocalTime.of(23, 59))
                        .scheduleType("HOLIDAY")
                        .color("#dc3545")
                        .isAllDay(true)
                        .isRecurring(false)
                        .priority(1)
                        .isFixed(true)
                        .isEditable(false)
                        .fixedReason("HOLIDAY")
                        .status("ACTIVE")
                        .build();
                
                holidaySchedules.add(holidaySchedule);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return holidaySchedules;
    }
    
    /**
     * 고정 스케줄 수정 방지 검증
     */
    private void validateScheduleEditability(Schedule schedule) {
        if (schedule.getIsFixed() && !schedule.getIsEditable()) {
            throw new RuntimeException("Cannot modify fixed schedule: " + schedule.getFixedReason());
        }
    }
    
    /**
     * 사용자별 스케줄 조회
     */
    public List<ScheduleResponseDto> getUserSchedules(Long userId) {
        try {
            List<Schedule> schedules = scheduleRepository.findByUserIdAndStatusOrderByStartDateAscStartTimeAsc(userId, "ACTIVE");
            return schedules.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching schedules for userId: {}", userId, e);
            throw new RuntimeException("Failed to fetch schedules", e);
        }
    }
    
    /**
     * 사용자별 특정 기간 스케줄 조회
     */
    public List<ScheduleResponseDto> getUserSchedulesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateRange(userId, "ACTIVE", startDate, endDate);
            return schedules.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching schedules for userId: {} between {} and {}", userId, startDate, endDate, e);
            throw new RuntimeException("Failed to fetch schedules", e);
        }
    }
    
    /**
     * 스케줄 상세 조회
     */
    public ScheduleResponseDto getScheduleById(Long userId, Long scheduleId) {
        try {
            Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            
            return convertToResponseDto(schedule);
        } catch (Exception e) {
            log.error("Error fetching schedule: {} for userId: {}", scheduleId, userId, e);
            throw new RuntimeException("Failed to fetch schedule", e);
        }
    }
    
    /**
     * 사용자별 근무 시간 조정 조회
     */
    public List<WorkTimeAdjustment> getUserWorkTimeAdjustments(Long userId) {
        try {
            return workTimeAdjustmentRepository.findByUserIdOrderByAdjustDateDesc(userId);
        } catch (Exception e) {
            log.error("Error fetching work time adjustments for userId: {}", userId, e);
            throw new RuntimeException("Failed to fetch work time adjustments", e);
        }
    }
    
    /**
     * 승인 대기 중인 근무 시간 조정 조회
     */
    public List<WorkTimeAdjustment> getPendingWorkTimeAdjustments() {
        try {
            return workTimeAdjustmentRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        } catch (Exception e) {
            log.error("Error fetching pending work time adjustments", e);
            throw new RuntimeException("Failed to fetch pending work time adjustments", e);
        }
    }
    
    /**
     * Schedule 엔티티를 ScheduleResponseDto로 변환
     */
    private ScheduleResponseDto convertToResponseDto(Schedule schedule) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .userId(schedule.getUserId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .scheduleType(schedule.getScheduleType())
                .color(schedule.getColor())
                .isAllDay(schedule.getIsAllDay())
                .isRecurring(schedule.getIsRecurring())
                .recurrencePattern(schedule.getRecurrencePattern())
                .recurrenceInterval(schedule.getRecurrenceInterval())
                .recurrenceDays(schedule.getRecurrenceDays())
                .recurrenceEndDate(schedule.getRecurrenceEndDate())
                .workPolicyId(schedule.getWorkPolicyId())
                .priority(schedule.getPriority())
                .location(schedule.getLocation())
                .attendees(schedule.getAttendees())
                .notes(schedule.getNotes())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .status(schedule.getStatus())
                .isFixed(schedule.getIsFixed())
                .isEditable(schedule.getIsEditable())
                .fixedReason(schedule.getFixedReason())
                .build();
    }
} 