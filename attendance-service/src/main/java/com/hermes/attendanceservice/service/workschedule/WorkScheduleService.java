package com.hermes.attendanceservice.service.workschedule;

import com.hermes.attendanceservice.client.UserServiceClient;
import com.hermes.attendanceservice.dto.workschedule.AdjustWorkTimeRequestDto;
import com.hermes.attendanceservice.dto.workschedule.ColleagueScheduleResponseDto;
import com.hermes.attendanceservice.dto.workschedule.CreateScheduleRequestDto;
import com.hermes.attendanceservice.dto.workschedule.ScheduleResponseDto;
import com.hermes.attendanceservice.dto.workschedule.UpdateScheduleRequestDto;
import com.hermes.attendanceservice.dto.workschedule.UserWorkPolicyDto;
import com.hermes.attendanceservice.dto.workschedule.WorkPolicyDto;
import com.hermes.attendanceservice.dto.workschedule.WorkTimeInfoDto;
import com.hermes.attendanceservice.entity.workschedule.Schedule;
import com.hermes.attendanceservice.entity.workschedule.ScheduleType;
import com.hermes.attendanceservice.entity.workschedule.WorkTimeAdjustment;
import com.hermes.attendanceservice.repository.workschedule.ScheduleRepository;
import com.hermes.attendanceservice.repository.workschedule.WorkTimeAdjustmentRepository;
import com.hermes.attendanceservice.service.workpolicy.WorkPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.hermes.attendanceservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.attendanceservice.entity.workpolicy.StartDayOfWeek;
import com.hermes.api.common.ApiResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkScheduleService {
    
    private final UserServiceClient userServiceClient;
    private final ScheduleRepository scheduleRepository;
    private final WorkTimeAdjustmentRepository workTimeAdjustmentRepository;
    private final WorkPolicyService workPolicyService; // WorkPolicyService 주입 추가
    
    /**
     * 특정 날짜의 사용자 근무 스케줄 조회
     */
    public Schedule getUserWorkSchedule(Long userId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findByUserIdAndDateAndScheduleType(userId, date, ScheduleType.WORK);
        return schedules.isEmpty() ? null : schedules.get(0);
    }
    
    /**
     * 사용자의 근무 시작/종료 시간 조회
     */
    public WorkTimeInfoDto getUserWorkTime(Long userId, LocalDate date) {
        Schedule schedule = getUserWorkSchedule(userId, date);
        
        if (schedule != null) {
            return WorkTimeInfoDto.builder()
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
        }
        
        // 스케줄이 없으면 근무 정책 사용 (하드코딩 제거: endTime 우선, 기본값 미사용)
        try {
            UserWorkPolicyDto userPolicy = getUserWorkPolicy(userId);
            if (userPolicy != null && userPolicy.getWorkPolicy() != null) {
                WorkPolicyDto workPolicy = userPolicy.getWorkPolicy();
                LocalTime startTime = workPolicy.getStartTime();
                LocalTime endTime = workPolicy.getEndTime();
                if (endTime == null && workPolicy.getWorkHours() != null && workPolicy.getWorkMinutes() != null && startTime != null) {
                    int totalMinutes = workPolicy.getWorkHours() * 60 + workPolicy.getWorkMinutes();
                    endTime = startTime.plusMinutes(totalMinutes);
                } else if (endTime == null && workPolicy.getWorkHours() != null && startTime != null) {
                    endTime = startTime.plusHours(workPolicy.getWorkHours());
                }
                return WorkTimeInfoDto.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
            }
        } catch (Exception e) {
            log.warn("Failed to get user work policy for userId: {}, date: {}", userId, date, e);
        }
        
        // 정책/스케줄에서 결정 불가 시 null 반환 (기본 하드코딩 제거)
        return WorkTimeInfoDto.builder()
            .startTime(null)
            .endTime(null)
            .build();
    }
    
    /**
     * 사용자 ID를 통해 해당 사용자의 근무 정책 정보를 조회
     */
    public UserWorkPolicyDto getUserWorkPolicy(Long userId) {
        try {
            // 1. User Service에서 사용자 정보 조회 (simple 우선, 실패 시 전체 조회로 폴백)
            Map<String, Object> userResponse = null;
            try {
                userResponse = userServiceClient.getUserWorkPolicy(userId); // /api/users/{userId}/simple
            } catch (Exception ignore) {}
            if (userResponse == null || userResponse.isEmpty()) {
                userResponse = userServiceClient.getUserById(userId); // /api/users/{userId}
            }
            
            if (userResponse == null || userResponse.isEmpty()) {
                log.warn("User not found with id: {} or response is empty", userId);
                return null;
            }
            
            // 2. workPolicyId 추출 (다양한 키/구조 대응)
            Long workPolicyId = null;
            Object workPolicyIdObj = null;
            
            // top-level 후보 키들
            String[] candidateKeys = new String[] {"workPolicyId", "work_policy_id", "workPolicyID"};
            for (String key : candidateKeys) {
                if (userResponse.containsKey(key) && userResponse.get(key) != null) {
                    workPolicyIdObj = userResponse.get(key);
                    break;
                }
            }
            
            // nested: workPolicy.id 또는 workPolicyId
            if (workPolicyIdObj == null) {
                Object wpObj = userResponse.get("workPolicy");
                if (wpObj instanceof Map<?, ?> wpMap) {
                    Object nestedId = ((Map<?, ?>) wpMap).get("id");
                    if (nestedId == null) {
                        nestedId = ((Map<?, ?>) wpMap).get("workPolicyId");
                    }
                    if (nestedId == null) {
                        nestedId = ((Map<?, ?>) wpMap).get("work_policy_id");
                    }
                    if (nestedId != null) {
                        workPolicyIdObj = nestedId;
                    }
                }
            }
            
            if (workPolicyIdObj != null) {
                try {
                    workPolicyId = Long.valueOf(workPolicyIdObj.toString());
                } catch (NumberFormatException nfe) {
                    log.warn("workPolicyId parse failed for userId {}: value={}", userId, workPolicyIdObj);
                }
            }
            
            // 이름 기반 폴백 (가능하다면)
            if (workPolicyId == null) {
                Object nameObj = userResponse.get("workPolicyName");
                if (nameObj == null) {
                    Object wpObj = userResponse.get("workPolicy");
                    if (wpObj instanceof Map<?, ?> wpMap) {
                        nameObj = ((Map<?, ?>) wpMap).get("name");
                    }
                }
                if (nameObj != null) {
                    try {
                        WorkPolicyResponseDto wpByName = workPolicyService.getWorkPolicyByName(nameObj.toString());
                        if (wpByName != null) {
                            workPolicyId = wpByName.getId();
                        }
                    } catch (Exception e) {
                        log.warn("Fallback by workPolicy name failed for userId {}: {}", userId, nameObj, e);
                    }
                }
            }
            
            if (workPolicyId == null) {
                log.warn("User {} has no work policy assigned (could not resolve workPolicyId)", userId);
                throw new RuntimeException("사용자에게 근무정책이 할당되지 않았습니다. 관리자에게 문의하세요.");
            }
            
            // 3. 근무 정책 정보 조회 (WorkPolicyService 사용)
            WorkPolicyResponseDto workPolicyResponse = workPolicyService.getWorkPolicyById(workPolicyId);
            
            if (workPolicyResponse == null) {
                log.warn("Work policy not found for workPolicyId: {}", workPolicyId);
                throw new RuntimeException("근무정책을 찾을 수 없습니다 (ID: " + workPolicyId + "). 관리자에게 문의하세요.");
            }
            
            // 4. WorkPolicyResponseDto를 WorkPolicyDto로 변환
            WorkPolicyDto workPolicy = convertToWorkPolicyDto(workPolicyResponse);
            
            return UserWorkPolicyDto.builder()
                    .workPolicyId(workPolicyId)
                    .workPolicy(workPolicy)
                    .build();
                    
        } catch (feign.FeignException.Unauthorized e) {
            log.error("Unauthorized access to user service for userId: {}. Please check JWT token.", userId);
            throw new RuntimeException("인증이 필요합니다. JWT 토큰을 확인해주세요.", e);
        } catch (feign.FeignException.ServiceUnavailable e) {
            log.error("User service is unavailable for userId: {}. Service may be down.", userId);
            throw new RuntimeException("사용자 서비스에 연결할 수 없습니다. 서비스가 실행 중인지 확인해주세요.", e);
        } catch (Exception e) {
            log.error("Error fetching user work policy for userId: {}", userId, e);
            throw new RuntimeException("사용자 근무 정책 조회에 실패했습니다.", e);
        }
    }
    

    
    /**
     * 새로운 스케줄 생성
     */
    @Transactional
    public ScheduleResponseDto createSchedule(CreateScheduleRequestDto requestDto, String authorization) {
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
                throw new RuntimeException("동일 시간대에 기존 스케줄이 존재합니다.");
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
                throw new RuntimeException("동일 시간대에 기존 스케줄이 존재합니다.");
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
            
            // 2. 기존 모든 스케줄 삭제 (해당 기간)
            deleteExistingSchedules(userId, startDate, endDate);
            
            // 3. 근무 타입별 스케줄 생성
            List<Schedule> allSchedules = new ArrayList<>();
            
            // 근무 타입에 따른 분기 처리
            switch (workPolicy.getType()) {
                case "FLEXIBLE":
                    // 선택근무: 코어타임 스케줄만 생성
                    List<Schedule> coreTimeSchedules = createCoreTimeSchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
                    allSchedules.addAll(coreTimeSchedules);
                    log.info("선택근무 타입: 코어타임 스케줄 {} 개 생성", coreTimeSchedules.size());
                    break;
                    
                case "SHIFT":
                    // 교대근무: 근무시간 + 휴게시간 블록 (자유 이동 가능)
                    List<Schedule> shiftWorkSchedules = createShiftWorkSchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
                    List<Schedule> shiftBreakSchedules = createShiftBreakSchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
                    allSchedules.addAll(shiftWorkSchedules);
                    allSchedules.addAll(shiftBreakSchedules);
                    log.info("교대근무 타입: 근무 {} 개, 휴게 {} 개 스케줄 생성", shiftWorkSchedules.size(), shiftBreakSchedules.size());
                    break;
                    
                case "STAGGERED":
                    // 시차근무: 근무시간 + 휴게시간 블록 (startTime-startTimeEnd 범위 내에서만 이동)
                    List<Schedule> staggeredWorkSchedules = createStaggeredWorkSchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
                    List<Schedule> staggeredBreakSchedules = createStaggeredBreakSchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
                    allSchedules.addAll(staggeredWorkSchedules);
                    allSchedules.addAll(staggeredBreakSchedules);
                    log.info("시차근무 타입: 근무 {} 개, 휴게 {} 개 스케줄 생성", staggeredWorkSchedules.size(), staggeredBreakSchedules.size());
                    break;
                    
                default:
                    // 기본 근무 타입 (FIXED 등): 기존 로직 유지
                    List<Schedule> workSchedules = createWorkDaySchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
                    List<Schedule> breakSchedules = createBreakTimeSchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
            allSchedules.addAll(workSchedules);
            allSchedules.addAll(breakSchedules);
                    log.info("기본 근무 타입: 근무 {} 개, 휴게 {} 개 스케줄 생성", workSchedules.size(), breakSchedules.size());
                    break;
            }
            
            // 4. 휴일 스케줄 생성 (모든 타입 공통)
            List<Schedule> holidaySchedules = createHolidaySchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
            allSchedules.addAll(holidaySchedules);
            
            List<Schedule> savedSchedules = scheduleRepository.saveAll(allSchedules);
            
            log.info("Created {} fixed schedules for userId: {} from {} to {} (Work Type: {})", 
                    savedSchedules.size(), userId, startDate, endDate, workPolicy.getType());
            
            return savedSchedules.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error creating fixed schedules for userId: {} from {} to {}", userId, startDate, endDate, e);
            throw new RuntimeException("Fixed schedule creation failed", e);
        }
    }

    /**
     * WorkPolicy 정보를 기반으로 Workschedule에 반영하여 저장
     * 근무시간, 휴게시간, 출근시간, 퇴근시간, 코어시간, 시차 근무 출근 가능 시간 등을 스케줄로 생성
     */
    @Transactional
    public List<ScheduleResponseDto> applyWorkPolicyToSchedule(Long userId, String authorization, LocalDate startDate, LocalDate endDate) {
        try {
            // 1. 사용자의 WorkPolicy 정보 조회
            UserWorkPolicyDto userWorkPolicy = getUserWorkPolicy(userId);
            
            if (userWorkPolicy == null || userWorkPolicy.getWorkPolicy() == null) {
                throw new RuntimeException("User has no work policy assigned");
            }
            
            WorkPolicyDto workPolicy = userWorkPolicy.getWorkPolicy();
            Long workPolicyId = userWorkPolicy.getWorkPolicyId();
            
            log.info("Applying work policy to schedule for userId: {}, workPolicyId: {}, period: {} to {}", 
                    userId, workPolicyId, startDate, endDate);
            
            // 2. 기존 모든 스케줄 삭제 (해당 기간)
            deleteExistingSchedules(userId, startDate, endDate);
            
            // 3. 근무 타입별 스케줄 생성
            List<Schedule> allSchedules = new ArrayList<>();
            
            if ("FLEXIBLE".equals(workPolicy.getType())) {
                // 시차 근무: startTime과 startTimeEnd 사이 랜덤 위치에 근무 블록 생성 + 휴게시간
                log.debug("Creating flexible work schedules for FLEXIBLE work policy: userId={}", userId);
                List<Schedule> flexibleWorkSchedules = createFlexibleWorkSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                List<Schedule> flexibleBreakSchedules = createFlexibleBreakTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                
                allSchedules.addAll(flexibleWorkSchedules);
                allSchedules.addAll(flexibleBreakSchedules);
                log.debug("Created {} flexible work schedules and {} break schedules for FLEXIBLE work", 
                        flexibleWorkSchedules.size(), flexibleBreakSchedules.size());
            } else if ("OPTIONAL".equals(workPolicy.getType())) {
                // 선택 근무: 코어타임만 생성
                log.debug("Creating core time schedules for OPTIONAL work policy: userId={}", userId);
                List<Schedule> coreTimeSchedules = createCoreTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                allSchedules.addAll(coreTimeSchedules);
                log.debug("Created {} core time schedules for OPTIONAL work", coreTimeSchedules.size());
            } else if ("SHIFT".equals(workPolicy.getType())) {
                // 교대근무: 랜덤 위치 근무 스케줄과 가운데 휴게시간 생성
                log.debug("Creating shift work schedules for SHIFT work policy: userId={}", userId);
                List<Schedule> workSchedules = createShiftWorkSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                List<Schedule> shiftBreakSchedules = createShiftBreakTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                
                allSchedules.addAll(workSchedules);
                allSchedules.addAll(shiftBreakSchedules);
                log.debug("Created {} work schedules and {} break schedules for SHIFT work", 
                        workSchedules.size(), shiftBreakSchedules.size());
            } else {
                // 기타 근무 타입: 기존 로직 유지
                log.debug("Creating work day schedules for userId: {}", userId);
                List<Schedule> workSchedules = createWorkDaySchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                log.debug("Created {} work day schedules", workSchedules.size());
                
                log.debug("Creating break time schedules for userId: {}", userId);
                List<Schedule> breakSchedules = createBreakTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                log.debug("Created {} break time schedules", breakSchedules.size());
                
                log.debug("Creating flexible work time schedules for userId: {}", userId);
                List<Schedule> flexibleSchedules = createFlexibleWorkTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
                log.debug("Created {} flexible work time schedules", flexibleSchedules.size());
                
                allSchedules.addAll(workSchedules);
                allSchedules.addAll(breakSchedules);
                allSchedules.addAll(flexibleSchedules);
            }
            
            // 4. 휴일 스케줄 생성 (모든 타입 공통)
            log.debug("Creating holiday schedules for userId: {}", userId);
            List<Schedule> holidaySchedules = createHolidaySchedules(userId, workPolicy, workPolicyId, startDate, endDate);
            log.debug("Created {} holiday schedules", holidaySchedules.size());
            allSchedules.addAll(holidaySchedules);
            
            // 5. 모든 스케줄 저장
            
            List<Schedule> savedSchedules = scheduleRepository.saveAll(allSchedules);
            
            log.info("Successfully applied work policy to schedule. Created {} schedules for userId: {} from {} to {}", 
                    savedSchedules.size(), userId, startDate, endDate);
            
            return savedSchedules.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error applying work policy to schedule for userId: {} from {} to {}", userId, startDate, endDate, e);
            throw new RuntimeException("Failed to apply work policy to schedule", e);
        }
    }
    
    /**
     * 기존 모든 스케줄 삭제 (고정 스케줄 + 사용자 생성 스케줄)
     */
    private void deleteExistingSchedules(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> existingSchedules = scheduleRepository.findByUserIdAndDateRange(userId, "ACTIVE", startDate, endDate);
        
        for (Schedule schedule : existingSchedules) {
            schedule.cancel();
        }
        scheduleRepository.saveAll(existingSchedules);
        
        log.info("Deleted {} existing schedules (fixed + user-created) for userId: {}", existingSchedules.size(), userId);
    }
    
    /**
     * 근무일 스케줄 생성 (출근시간, 퇴근시간, 근무시간 반영)
     */
    private List<Schedule> createWorkDaySchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> workSchedules = new ArrayList<>();
        
        log.debug("Creating work day schedules: userId={}, workPolicyId={}, startDate={}, endDate={}", 
                userId, workPolicyId, startDate, endDate);
        
        if (workPolicy.getWorkDays() == null || workPolicy.getWorkDays().isEmpty()) {
            log.warn("No work days defined in work policy, using default Mon-Fri: userId={}, workPolicyId={}", userId, workPolicyId);
            // 기본 근무 요일을 월~금으로 설정
            workPolicy.setWorkDays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"));
        }
        
        if (workPolicy.getStartTime() == null) {
            log.warn("No start time defined in work policy, using default 09:00: userId={}, workPolicyId={}", userId, workPolicyId);
            // 기본 출근 시간을 09:00으로 설정 (선택 근무가 아닌 경우에만)
            workPolicy.setStartTime(LocalTime.of(9, 0));
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays().contains(dayOfWeek)) {
                // 출근 시간
                LocalTime startTime = workPolicy.getStartTime();
                
                // 퇴근 시간: 정책 endTime 우선 사용, 없으면 기존 계산식으로 폴백 (startTime이 있을 때만)
                LocalTime endTime = workPolicy.getEndTime();
                if (endTime == null && startTime != null) {
                    if (workPolicy.getWorkHours() != null && workPolicy.getWorkMinutes() != null) {
                        endTime = startTime.plusHours(workPolicy.getWorkHours()).plusMinutes(workPolicy.getWorkMinutes());
                    } else {
                        endTime = startTime.plusHours(8); // 기본 8시간 근무
                    }
                }
                
                if (endTime == null) {
                    log.warn("Could not determine end time, using default 8 hours: userId={}, date={}, startTime={}", 
                            userId, currentDate, startTime);
                    // 기본 8시간 근무로 설정
                    endTime = startTime.plusHours(8);
                }
                
                try {
                    Schedule workSchedule = Schedule.builder()
                            .userId(userId)
                            .title(ScheduleType.WORK.getDescription())
                            .description(String.format("출근: %s, 퇴근: %s", startTime, endTime))
                            .startDate(currentDate)
                            .endDate(currentDate)
                            .startTime(startTime)
                            .endTime(endTime)
                            .scheduleType(ScheduleType.WORK)
                            .color("#007bff")
                            .isAllDay(false)
                            .isRecurring(false)
                            .workPolicyId(workPolicyId)
                            .priority(1)
                            .isFixed(true)
                            .isEditable(false)
                            .fixedReason("WORK_POLICY")
                            .status("ACTIVE")
                            .build();
                    
                    workSchedules.add(workSchedule);
                    log.debug("Created work schedule: userId={}, date={}, time={}~{}", 
                            userId, currentDate, startTime, endTime);
                } catch (Exception e) {
                    log.error("Error creating work schedule: userId={}, date={}, startTime={}, endTime={}", 
                            userId, currentDate, startTime, endTime, e);
                    throw new RuntimeException("근무 스케줄 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return workSchedules;
    }

    /**
     * 시차 근무 스케줄 생성 (startTime과 startTimeEnd 사이 랜덤 배치)
     */
    private List<Schedule> createFlexibleWorkSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> flexibleSchedules = new ArrayList<>();
        
        log.debug("Creating flexible work schedules: userId={}, workPolicyId={}, startDate={}, endDate={}", 
                userId, workPolicyId, startDate, endDate);
        
        if (workPolicy.getWorkDays() == null || workPolicy.getWorkDays().isEmpty()) {
            log.warn("No work days defined in work policy, using default Mon-Fri: userId={}, workPolicyId={}", userId, workPolicyId);
            // 기본 근무 요일을 월~금으로 설정
            workPolicy.setWorkDays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"));
        }
        
        // 시차 근무 시간 범위 확인
        LocalTime startTime = workPolicy.getStartTime();
        LocalTime startTimeEnd = workPolicy.getStartTimeEnd();
        
        if (startTime == null || startTimeEnd == null) {
            log.warn("시차 근무 시간 범위가 설정되지 않음. 기본값 사용: userId={}, workPolicyId={}", userId, workPolicyId);
            startTime = LocalTime.of(7, 0);  // 기본 시작 가능 시간: 07:00
            startTimeEnd = LocalTime.of(10, 0); // 기본 시작 마감 시간: 10:00
        }
        
        // 근무 시간 설정
        int workHours = 8; // 기본 8시간 근무
        if (workPolicy.getWorkHours() != null) {
            workHours = workPolicy.getWorkHours();
        }
        
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays().contains(dayOfWeek)) {
                try {
                    // 날짜별 일관된 랜덤 시드 사용 (휴게시간과 동일한 근무 시간 보장)
                    Random dayRandom = new Random(currentDate.toEpochDay());
                    
                    // startTime과 startTimeEnd 사이에서 랜덤한 시작 시간 선택
                    long startMinutes = startTime.toSecondOfDay() / 60;
                    long endMinutes = startTimeEnd.toSecondOfDay() / 60;
                    
                    if (endMinutes <= startMinutes) {
                        log.warn("시차 근무 시간 범위가 잘못됨: startTime={}, startTimeEnd={}. 기본 범위 사용", startTime, startTimeEnd);
                        startMinutes = 7 * 60; // 07:00
                        endMinutes = 10 * 60;  // 10:00
                    }
                    
                    // 랜덤 시작 시간 계산
                    long randomMinutes = startMinutes + dayRandom.nextLong(endMinutes - startMinutes + 1);
                    LocalTime randomStartTime = LocalTime.ofSecondOfDay(randomMinutes * 60);
                    
                    // 종료 시간 계산
                    LocalTime endTime = randomStartTime.plusHours(workHours);
                    
                    // 자정을 넘지 않도록 조정
                    if (endTime.isAfter(LocalTime.of(23, 59))) {
                        endTime = LocalTime.of(23, 59);
                        randomStartTime = endTime.minusHours(workHours);
                    }
                    
                    Schedule flexibleSchedule = Schedule.builder()
                            .userId(userId)
                            .title(ScheduleType.WORK.getDescription())
                            .description(String.format("시차근무: %s ~ %s (자율 출근)", randomStartTime, endTime))
                            .startDate(currentDate)
                            .endDate(currentDate)
                            .startTime(randomStartTime)
                            .endTime(endTime)
                            .scheduleType(ScheduleType.WORK)
                            .color("#28a745")
                            .isAllDay(false)
                            .isRecurring(false)
                            .workPolicyId(workPolicyId)
                            .priority(1)
                            .isFixed(true)
                            .isEditable(true) // 시차근무는 시간 조정 가능
                            .fixedReason("FLEXIBLE_WORK_TIME")
                            .status("ACTIVE")
                            .build();
                    
                    flexibleSchedules.add(flexibleSchedule);
                    log.debug("시차근무 스케줄 생성: userId={}, date={}, time={}~{}", 
                            userId, currentDate, randomStartTime, endTime);
                    
                } catch (Exception e) {
                    log.error("시차근무 스케줄 생성 오류: userId={}, date={}", userId, currentDate, e);
                    throw new RuntimeException("시차근무 스케줄 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("시차근무 스케줄 {} 개 생성 완료: userId={}", flexibleSchedules.size(), userId);
        return flexibleSchedules;
    }

    /**
     * 시차 근무 휴게시간 스케줄 생성 (각 근무 블록의 가운데에 배치)
     */
    private List<Schedule> createFlexibleBreakTimeSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> breakSchedules = new ArrayList<>();
        
        log.debug("Creating flexible break time schedules: userId={}, workPolicyId={}, startDate={}, endDate={}", 
                userId, workPolicyId, startDate, endDate);
        
        if (workPolicy.getWorkDays() == null || workPolicy.getWorkDays().isEmpty()) {
            log.warn("No work days defined in work policy: userId={}, workPolicyId={}", userId, workPolicyId);
            return breakSchedules;
        }
        
        // 시차 근무 시간 범위 확인
        LocalTime startTime = workPolicy.getStartTime();
        LocalTime startTimeEnd = workPolicy.getStartTimeEnd();
        
        if (startTime == null || startTimeEnd == null) {
            log.warn("시차 근무 시간 범위가 설정되지 않음. 휴게시간 생성 불가: userId={}, workPolicyId={}", userId, workPolicyId);
            return breakSchedules;
        }
        
        // 근무 시간 설정
        int workHours = 8; // 기본 8시간 근무
        if (workPolicy.getWorkHours() != null) {
            workHours = workPolicy.getWorkHours();
        }
        
        // 휴게시간 설정 (기본값: 1시간)
        int breakDurationMinutes = 60;
        if (workPolicy.getBreakMinutes() != null && workPolicy.getBreakMinutes() > 0) {
            breakDurationMinutes = workPolicy.getBreakMinutes();
        }
        
        LocalDate currentDate = startDate;
        Random random = new Random();
        
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays().contains(dayOfWeek)) {
                try {
                    // 해당 날짜의 근무 시작 시간을 동일하게 계산 (랜덤 시드를 날짜 기반으로 설정)
                    Random dayRandom = new Random(currentDate.toEpochDay());
                    
                    long startMinutes = startTime.toSecondOfDay() / 60;
                    long endMinutes = startTimeEnd.toSecondOfDay() / 60;
                    
                    if (endMinutes <= startMinutes) {
                        startMinutes = 7 * 60; // 07:00
                        endMinutes = 10 * 60;  // 10:00
                    }
                    
                    // 해당 날짜의 근무 시작 시간 계산 (createFlexibleWorkSchedules와 동일한 로직)
                    long randomMinutes = startMinutes + dayRandom.nextLong(endMinutes - startMinutes + 1);
                    LocalTime workStartTime = LocalTime.ofSecondOfDay(randomMinutes * 60);
                    LocalTime workEndTime = workStartTime.plusHours(workHours);
                    
                    // 자정을 넘지 않도록 조정
                    if (workEndTime.isAfter(LocalTime.of(23, 59))) {
                        workEndTime = LocalTime.of(23, 59);
                        workStartTime = workEndTime.minusHours(workHours);
                    }
                    
                    // 근무 시간의 가운데에 휴게시간 배치
                    long workMinutes = Duration.between(workStartTime, workEndTime).toMinutes();
                    long halfWorkMinutes = workMinutes / 2;
                    
                    LocalTime breakStartTime = workStartTime.plusMinutes(halfWorkMinutes - (breakDurationMinutes / 2));
                    LocalTime breakEndTime = breakStartTime.plusMinutes(breakDurationMinutes);
                    
                    // 휴게시간이 근무시간을 벗어나지 않도록 조정
                    if (breakStartTime.isBefore(workStartTime)) {
                        breakStartTime = workStartTime.plusMinutes(30);
                        breakEndTime = breakStartTime.plusMinutes(breakDurationMinutes);
                    }
                    if (breakEndTime.isAfter(workEndTime)) {
                        breakEndTime = workEndTime.minusMinutes(30);
                        breakStartTime = breakEndTime.minusMinutes(breakDurationMinutes);
                    }
                    
                    Schedule breakSchedule = Schedule.builder()
                            .userId(userId)
                            .title(ScheduleType.RESTTIME.getDescription())
                            .description(String.format("시차근무 휴게시간: %s ~ %s", breakStartTime, breakEndTime))
                            .startDate(currentDate)
                            .endDate(currentDate)
                            .startTime(breakStartTime)
                            .endTime(breakEndTime)
                            .scheduleType(ScheduleType.RESTTIME)
                            .color("#ffc107")
                            .isAllDay(false)
                            .isRecurring(false)
                            .workPolicyId(workPolicyId)
                            .priority(2)
                            .isFixed(true)
                            .isEditable(false)
                            .fixedReason("FLEXIBLE_BREAK_TIME")
                            .status("ACTIVE")
                            .build();
                    
                    breakSchedules.add(breakSchedule);
                    log.debug("시차근무 휴게시간 생성: userId={}, date={}, breakTime={}~{}", 
                            userId, currentDate, breakStartTime, breakEndTime);
                    
                } catch (Exception e) {
                    log.error("시차근무 휴게시간 생성 오류: userId={}, date={}", userId, currentDate, e);
                    throw new RuntimeException("시차근무 휴게시간 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("시차근무 휴게시간 {} 개 생성 완료: userId={}", breakSchedules.size(), userId);
        return breakSchedules;
    }
    
    /**
     * 휴식 시간 스케줄 생성 (휴게시간 반영)
     */
    private List<Schedule> createBreakTimeSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> breakSchedules = new ArrayList<>();
        
        if (workPolicy.getBreakStartTime() == null) {
            return breakSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                // 정책에서 계산된 breakEndTime을 그대로 사용, 없으면 스킵
                LocalTime breakEndTime = workPolicy.getBreakEndTime();
                if (breakEndTime == null) {
                    log.debug("breakEndTime is null. Skipping break schedule. userId={}, date={}", userId, currentDate);
                    currentDate = currentDate.plusDays(1);
                    continue;
                }
                
                Schedule breakSchedule = Schedule.builder()
                        .userId(userId)
                        .title(ScheduleType.RESTTIME.getDescription())
                        .description(String.format("휴게시간: %s ~ %s", 
                                workPolicy.getBreakStartTime(), breakEndTime))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getBreakStartTime())
                        .endTime(breakEndTime)
                        .scheduleType(ScheduleType.RESTTIME)
                        .color("#ffc107")
                        .isAllDay(false)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
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
     * 교대근무 휴게시간 스케줄 생성 (근무 시간 가운데에 휴게시간 배치)
     */
    private List<Schedule> createShiftBreakTimeSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> breakSchedules = new ArrayList<>();
        
        // 교대근무에서는 근무시간이 설정되어 있어야 휴게시간 계산 가능
        if (workPolicy.getStartTime() == null || workPolicy.getEndTime() == null) {
            log.debug("Start time or end time is null for SHIFT work. Cannot create break schedules. userId={}", userId);
            return breakSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                LocalTime workStartTime = workPolicy.getStartTime();
                LocalTime workEndTime = workPolicy.getEndTime();
                
                // 근무 시간의 가운데 시점 계산
                long workMinutes = Duration.between(workStartTime, workEndTime).toMinutes();
                long halfWorkMinutes = workMinutes / 2;
                
                // 휴게시간 설정 (기본값: 1시간)
                int breakDurationMinutes = 60;
                if (workPolicy.getBreakMinutes() != null && workPolicy.getBreakMinutes() > 0) {
                    breakDurationMinutes = workPolicy.getBreakMinutes();
                }
                
                // 휴게시간 시작: 근무 시간의 정확히 가운데에 배치
                LocalTime breakStartTime = workStartTime.plusMinutes(halfWorkMinutes - (breakDurationMinutes / 2));
                LocalTime breakEndTime = breakStartTime.plusMinutes(breakDurationMinutes);
                
                // 휴게시간이 근무시간을 벗어나지 않도록 조정
                if (breakStartTime.isBefore(workStartTime)) {
                    breakStartTime = workStartTime.plusMinutes(30); // 최소 30분 후
                    breakEndTime = breakStartTime.plusMinutes(breakDurationMinutes);
                }
                if (breakEndTime.isAfter(workEndTime)) {
                    breakEndTime = workEndTime.minusMinutes(30); // 최소 30분 전
                    breakStartTime = breakEndTime.minusMinutes(breakDurationMinutes);
                }
                
                Schedule breakSchedule = Schedule.builder()
                        .userId(userId)
                        .title(ScheduleType.RESTTIME.getDescription())
                        .description(String.format("교대근무 휴게시간: %s ~ %s", breakStartTime, breakEndTime))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(breakStartTime)
                        .endTime(breakEndTime)
                        .scheduleType(ScheduleType.RESTTIME)
                        .color("#ffc107")
                        .isAllDay(false)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
                        .priority(2)
                        .isFixed(true)
                        .isEditable(false)
                        .fixedReason("SHIFT_BREAK_TIME")
                        .status("ACTIVE")
                        .build();
                
                breakSchedules.add(breakSchedule);
                log.debug("Created shift break schedule: userId={}, date={}, breakTime={}~{}", 
                        userId, currentDate, breakStartTime, breakEndTime);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return breakSchedules;
    }
    
    /**
     * 휴일 스케줄 생성 (휴일 반영)
     */
    private List<Schedule> createHolidaySchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
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
                        .scheduleType(ScheduleType.VACATION) // HOLIDAY는 VACATION으로 처리
                        .color("#dc3545")
                        .isAllDay(true)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
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
     * 코어 타임 스케줄 생성 (코어타임이 설정된 경우)
     */
    private List<Schedule> createCoreTimeSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> coreTimeSchedules = new ArrayList<>();
        
        // OPTIONAL 타입에서만 코어타임 생성 (FLEXIBLE은 시차근무이므로 코어타임 없음)
        if (!"OPTIONAL".equals(workPolicy.getType())) {
            log.debug("OPTIONAL 타입이 아니므로 코어타임 생성하지 않음. workType: {}, userId: {}, workPolicyId: {}", 
                    workPolicy.getType(), userId, workPolicyId);
            return coreTimeSchedules;
        }
        
        // 코어타임 시작/종료 시간이 설정되어 있어야 함
        if (workPolicy.getCoreTimeStart() == null || workPolicy.getCoreTimeEnd() == null) {
            // 선택 근무(OPTIONAL)의 경우 기본 코어타임 설정 (10:00 ~ 15:00)
            log.warn("코어타임이 설정되지 않음. 기본 코어타임(10:00-15:00) 사용. userId: {}, workPolicyId: {}", userId, workPolicyId);
            workPolicy.setCoreTimeStart(LocalTime.of(10, 0));
            workPolicy.setCoreTimeEnd(LocalTime.of(15, 0));
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                Schedule coreTimeSchedule = Schedule.builder()
                        .userId(userId)
                        .title(ScheduleType.CORETIME.getDescription())
                        .description(String.format("필수 근무시간: %s ~ %s (근무정책: %s)", 
                                workPolicy.getCoreTimeStart(), workPolicy.getCoreTimeEnd(), workPolicy.getType()))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getCoreTimeStart())
                        .endTime(workPolicy.getCoreTimeEnd())
                        .scheduleType(ScheduleType.CORETIME)
                        .color("#28a745")
                        .isAllDay(false)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
                        .priority(3)
                        .isFixed(true)
                        .isEditable(false)
                        .fixedReason("CORE_TIME")
                        .status("ACTIVE")
                        .build();
                
                coreTimeSchedules.add(coreTimeSchedule);
                
                log.debug("코어타임 스케줄 생성: userId={}, date={}, time={}~{}, workType={}", 
                        userId, currentDate, workPolicy.getCoreTimeStart(), workPolicy.getCoreTimeEnd(), workPolicy.getType());
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("코어타임 스케줄 {} 개 생성 완료: userId={}, workType={}", 
                coreTimeSchedules.size(), userId, workPolicy.getType());
        return coreTimeSchedules;
    }

    /**
     * 시차 근무 출근 가능 시간대 스케줄 생성
     * 시차 근무의 경우 출근 가능 시간대(startTime ~ startTimeEnd)를 설정할 수 있음
     */
    private List<Schedule> createFlexibleWorkTimeSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> flexibleSchedules = new ArrayList<>();
        
        // 시차 근무이고 출근 가능 시간대가 설정된 경우에만 생성
        if (!"FLEXIBLE".equals(workPolicy.getType())) {
            return flexibleSchedules; // 시차 근무가 아닌 경우 스케줄 생성하지 않음
        }
        
        // 시차 근무인 경우 startTime과 startTimeEnd가 필수
        if (workPolicy.getStartTime() == null || workPolicy.getStartTimeEnd() == null) {
            log.warn("시차 근무이지만 출근 가능 시간대가 설정되지 않음. userId: {}, workPolicyId: {}", userId, workPolicyId);
            return flexibleSchedules;
        }
        
        // 출근 시작 시간이 출근 종료 시간보다 이전인지 검증
        if (workPolicy.getStartTime().isAfter(workPolicy.getStartTimeEnd()) || 
            workPolicy.getStartTime().equals(workPolicy.getStartTimeEnd())) {
            log.warn("시차 근무의 출근 시간 설정이 유효하지 않음. startTime: {}, startTimeEnd: {}, userId: {}", 
                    workPolicy.getStartTime(), workPolicy.getStartTimeEnd(), userId);
            return flexibleSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                Schedule flexibleSchedule = Schedule.builder()
                        .userId(userId)
                        .title("시차 근무 - 출근 가능 시간대")
                        .description(String.format("출근 가능 시간: %s ~ %s (시차 근무)", 
                                workPolicy.getStartTime(), workPolicy.getStartTimeEnd()))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getStartTime())
                        .endTime(workPolicy.getStartTimeEnd())
                        .scheduleType(ScheduleType.WORK) // FLEXIBLE_WORK_TIME은 WORK로 처리
                        .color("#17a2b8")
                        .isAllDay(false)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
                        .priority(2)
                        .isFixed(true)
                        .isEditable(false)
                        .fixedReason("FLEXIBLE_WORK_TIME")
                        .status("ACTIVE")
                        .build();
                
                flexibleSchedules.add(flexibleSchedule);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return flexibleSchedules;
    }
    
    /**
     * 교대근무 타입 근무 스케줄 생성 (자유 이동 가능)
     */
    private List<Schedule> createShiftWorkSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> shiftWorkSchedules = new ArrayList<>();
        
        if (workPolicy.getWorkDays() == null || workPolicy.getWorkDays().isEmpty()) {
            log.warn("No work days defined in work policy, using default Mon-Fri: userId={}, workPolicyId={}", userId, workPolicyId);
            // 기본 근무 요일을 월~금으로 설정
            workPolicy.setWorkDays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"));
        }
        
        // 교대근무 기본 설정
        LocalTime baseStartTime = LocalTime.of(9, 0); // 기본 시작 시간
        int workHours = 8; // 기본 근무 시간
        
        if (workPolicy.getStartTime() != null) {
            baseStartTime = workPolicy.getStartTime();
        }
        if (workPolicy.getWorkHours() != null) {
            workHours = workPolicy.getWorkHours();
        }
        
        LocalDate currentDate = startDate;
        Random random = new Random();
        
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays().contains(dayOfWeek)) {
                try {
                    // 랜덤 시작 시간 계산 (기본 시간 ±3시간 범위)
                    int randomHourOffset = random.nextInt(7) - 3; // -3 ~ +3 시간
                    int randomMinuteOffset = random.nextInt(121) - 60; // -60 ~ +60 분
                    
                    LocalTime startTime = baseStartTime.plusHours(randomHourOffset).plusMinutes(randomMinuteOffset);
                    
                    // 시간 범위 제한 (06:00 ~ 22:00 사이)
                    if (startTime.isBefore(LocalTime.of(6, 0))) {
                        startTime = LocalTime.of(6, 0);
                    }
                    if (startTime.isAfter(LocalTime.of(22, 0))) {
                        startTime = LocalTime.of(22, 0);
                    }
                    
                    // 종료 시간 계산
                    LocalTime endTime = startTime.plusHours(workHours);
                    
                    // 자정을 넘지 않도록 조정
                    if (endTime.isAfter(LocalTime.of(23, 59))) {
                        endTime = LocalTime.of(23, 59);
                        startTime = endTime.minusHours(workHours);
                    }
                    
                    Schedule shiftWorkSchedule = Schedule.builder()
                            .userId(userId)
                            .title(ScheduleType.WORK.getDescription())
                            .description(String.format("교대근무: %s ~ %s (랜덤 배치)", startTime, endTime))
                            .startDate(currentDate)
                            .endDate(currentDate)
                            .startTime(startTime)
                            .endTime(endTime)
                            .scheduleType(ScheduleType.WORK)
                            .color("#007bff")
                            .isAllDay(false)
                            .isRecurring(false)
                            .workPolicyId(workPolicyId)
                            .priority(1)
                            .isFixed(true)
                            .isEditable(true) // 교대근무는 자유 이동 가능
                            .fixedReason("SHIFT_WORK_RANDOM")
                            .status("ACTIVE")
                            .build();
                    
                    shiftWorkSchedules.add(shiftWorkSchedule);
                    log.debug("교대근무 랜덤 스케줄 생성: userId={}, date={}, time={}~{}", 
                            userId, currentDate, startTime, endTime);
                    
                    // 휴게시간 계산을 위해 workPolicy 업데이트 (각 날짜별로 다른 근무시간 적용)
                    workPolicy.setStartTime(startTime);
                    workPolicy.setEndTime(endTime);
                    
                } catch (Exception e) {
                    log.error("교대근무 랜덤 스케줄 생성 오류: userId={}, date={}", userId, currentDate, e);
                    throw new RuntimeException("교대근무 스케줄 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("교대근무 랜덤 스케줄 {} 개 생성 완료: userId={}", shiftWorkSchedules.size(), userId);
        return shiftWorkSchedules;
    }
    
    /**
     * 교대근무 타입 휴게시간 스케줄 생성 (자유 이동 가능)
     */
    private List<Schedule> createShiftBreakSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> shiftBreakSchedules = new ArrayList<>();
        
        // 휴게시간이 설정되지 않은 경우 스케줄 생성하지 않음
        if (workPolicy.getBreakStartTime() == null || workPolicy.getBreakMinutes() == null) {
            log.debug("휴게시간이 설정되지 않음. userId: {}, workPolicyId: {}", userId, workPolicyId);
            return shiftBreakSchedules;
        }
        
        LocalTime breakEndTime = workPolicy.getBreakStartTime().plusMinutes(workPolicy.getBreakMinutes());
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                Schedule shiftBreakSchedule = Schedule.builder()
                        .userId(userId)
                        .title(ScheduleType.RESTTIME.getDescription())
                        .description(String.format("교대근무 휴게시간: %s ~ %s (자유 이동 가능)", 
                                workPolicy.getBreakStartTime(), breakEndTime))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getBreakStartTime())
                        .endTime(breakEndTime)
                        .scheduleType(ScheduleType.RESTTIME)
                        .color("#ffc107")
                        .isAllDay(false)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
                        .priority(2)
                        .isFixed(true)
                        .isEditable(true) // 교대근무는 자유 이동 가능
                        .fixedReason("SHIFT_BREAK")
                        .status("ACTIVE")
                        .build();
                
                shiftBreakSchedules.add(shiftBreakSchedule);
                
                log.debug("교대근무 휴게시간 스케줄 생성: userId={}, date={}, time={}~{}", 
                        userId, currentDate, workPolicy.getBreakStartTime(), breakEndTime);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("교대근무 휴게시간 스케줄 {} 개 생성 완료: userId={}", shiftBreakSchedules.size(), userId);
        return shiftBreakSchedules;
    }
    
    /**
     * 시차근무 타입 근무 스케줄 생성 (startTime-startTimeEnd 범위 내에서만 이동 가능)
     */
    private List<Schedule> createStaggeredWorkSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> staggeredWorkSchedules = new ArrayList<>();
        
        if (workPolicy.getWorkDays() == null || workPolicy.getWorkDays().isEmpty()) {
            return staggeredWorkSchedules;
        }
        
        // 시차근무는 startTime과 startTimeEnd가 필수
        if (workPolicy.getStartTime() == null || workPolicy.getStartTimeEnd() == null) {
            log.warn("시차근무이지만 출근 가능 시간대가 설정되지 않음. userId: {}, workPolicyId: {}", userId, workPolicyId);
            return staggeredWorkSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays().contains(dayOfWeek)) {
                // 시차근무 기본 출근 시간 (startTime 사용)
                LocalTime startTime = workPolicy.getStartTime();
                
                // 퇴근 시간: 정책 endTime 우선 사용, 없으면 근무시간으로 계산
                LocalTime endTime = workPolicy.getEndTime();
                if (endTime == null && startTime != null) {
                    if (workPolicy.getWorkHours() != null && workPolicy.getWorkMinutes() != null) {
                        endTime = startTime.plusHours(workPolicy.getWorkHours()).plusMinutes(workPolicy.getWorkMinutes());
                    } else {
                        endTime = startTime.plusHours(8); // 기본 8시간 근무
                    }
                }
                
                Schedule staggeredWorkSchedule = Schedule.builder()
                        .userId(userId)
                        .title(ScheduleType.WORK.getDescription())
                        .description(String.format("시차근무: %s ~ %s (출근시간 조정가능: %s~%s)", 
                                startTime, endTime, workPolicy.getStartTime(), workPolicy.getStartTimeEnd()))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .scheduleType(ScheduleType.WORK)
                        .color("#17a2b8")
                        .isAllDay(false)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
                        .priority(1)
                        .isFixed(true)
                        .isEditable(true) // 시차근무는 제한된 범위 내에서 이동 가능
                        .fixedReason("STAGGERED_WORK")
                        .status("ACTIVE")
                        .notes(String.format("출근시간 조정 가능 범위: %s ~ %s", 
                                workPolicy.getStartTime(), workPolicy.getStartTimeEnd()))
                        .build();
                
                staggeredWorkSchedules.add(staggeredWorkSchedule);
                
                log.debug("시차근무 스케줄 생성: userId={}, date={}, time={}~{}, 조정가능범위={}~{}", 
                        userId, currentDate, startTime, endTime, 
                        workPolicy.getStartTime(), workPolicy.getStartTimeEnd());
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("시차근무 스케줄 {} 개 생성 완료: userId={}", staggeredWorkSchedules.size(), userId);
        return staggeredWorkSchedules;
    }
    
    /**
     * 시차근무 타입 휴게시간 스케줄 생성
     */
    private List<Schedule> createStaggeredBreakSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> staggeredBreakSchedules = new ArrayList<>();
        
        // 휴게시간이 설정되지 않은 경우 스케줄 생성하지 않음
        if (workPolicy.getBreakStartTime() == null || workPolicy.getBreakMinutes() == null) {
            log.debug("휴게시간이 설정되지 않음. userId: {}, workPolicyId: {}", userId, workPolicyId);
            return staggeredBreakSchedules;
        }
        
        LocalTime breakEndTime = workPolicy.getBreakStartTime().plusMinutes(workPolicy.getBreakMinutes());
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                Schedule staggeredBreakSchedule = Schedule.builder()
                        .userId(userId)
                        .title(ScheduleType.RESTTIME.getDescription())
                        .description(String.format("시차근무 휴게시간: %s ~ %s", 
                                workPolicy.getBreakStartTime(), breakEndTime))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getBreakStartTime())
                        .endTime(breakEndTime)
                        .scheduleType(ScheduleType.RESTTIME)
                        .color("#fd7e14")
                        .isAllDay(false)
                        .isRecurring(false)
                        .workPolicyId(workPolicyId)
                        .priority(2)
                        .isFixed(true)
                        .isEditable(true) // 시차근무 휴게시간도 이동 가능
                        .fixedReason("STAGGERED_BREAK")
                        .status("ACTIVE")
                        .build();
                
                staggeredBreakSchedules.add(staggeredBreakSchedule);
                
                log.debug("시차근무 휴게시간 스케줄 생성: userId={}, date={}, time={}~{}", 
                        userId, currentDate, workPolicy.getBreakStartTime(), breakEndTime);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("시차근무 휴게시간 스케줄 {} 개 생성 완료: userId={}", staggeredBreakSchedules.size(), userId);
        return staggeredBreakSchedules;
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
     * 사용자별 스케줄 조회 (WorkPolicy 정보 포함)
     */
    public List<ScheduleResponseDto> getUserSchedules(Long userId, String authorization) {
        try {
            List<Schedule> schedules = scheduleRepository.findByUserIdAndStatusOrderByStartDateAscStartTimeAsc(userId, "ACTIVE");
            
            // WorkPolicy 정보도 함께 조회하여 스케줄에 추가 정보 제공
            UserWorkPolicyDto userWorkPolicy = getUserWorkPolicy(userId);
            
            return schedules.stream()
                    .map(schedule -> {
                        ScheduleResponseDto responseDto = convertToResponseDto(schedule);
                        
                        // WorkPolicy 정보가 있고, 해당 스케줄이 WorkPolicy와 연관된 경우 추가 정보 제공
                        if (userWorkPolicy != null && userWorkPolicy.getWorkPolicy() != null && 
                            schedule.getWorkPolicyId() != null && 
                            schedule.getWorkPolicyId().equals(userWorkPolicy.getWorkPolicyId())) {
                            
                            WorkPolicyDto workPolicy = userWorkPolicy.getWorkPolicy();
                            String additionalInfo = String.format("근무정책: %s (출근: %s, 퇴근: %s, 휴게: %s)", 
                                    workPolicy.getName(),
                                    workPolicy.getStartTime(),
                                    workPolicy.getStartTime() != null ? 
                                        workPolicy.getStartTime().plusHours(workPolicy.getWorkHours() != null ? workPolicy.getWorkHours() : 8)
                                                 .plusMinutes(workPolicy.getWorkMinutes() != null ? workPolicy.getWorkMinutes() : 0) : null,
                                    workPolicy.getBreakStartTime());
                            
                            // description에 추가 정보 포함
                            if (responseDto.getDescription() != null && !responseDto.getDescription().isEmpty()) {
                                responseDto.setDescription(responseDto.getDescription() + " | " + additionalInfo);
                            } else {
                                responseDto.setDescription(additionalInfo);
                            }
                        }
                        
                        return responseDto;
                    })
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
    
    /**
     * WorkPolicyResponseDto를 WorkPolicyDto로 변환
     */
    private WorkPolicyDto convertToWorkPolicyDto(WorkPolicyResponseDto responseDto) {
        try {
            log.debug("Converting WorkPolicyResponseDto to WorkPolicyDto: id={}, name={}, type={}", 
                    responseDto.getId(), responseDto.getName(), responseDto.getType());
            
            return WorkPolicyDto.builder()
                    .id(responseDto.getId())
                    .name(responseDto.getName())
                    .type(responseDto.getType() != null ? responseDto.getType().toString() : null)
                    .workCycle(responseDto.getWorkCycle() != null ? responseDto.getWorkCycle().toString() : null)
                    .startDayOfWeek(responseDto.getStartDayOfWeek() != null ? responseDto.getStartDayOfWeek().toString() : null)
                    .workCycleStartDay(responseDto.getWorkCycleStartDay())
                    .workDays(responseDto.getWorkDays() != null ? 
                        responseDto.getWorkDays().stream()
                            .filter(java.util.Objects::nonNull)
                            .map(StartDayOfWeek::toString)
                            .collect(java.util.stream.Collectors.toList()) : null)
                    .holidayDays(responseDto.getHolidayDays() != null ? 
                        responseDto.getHolidayDays().stream()
                            .filter(java.util.Objects::nonNull)
                            .map(StartDayOfWeek::toString)
                            .collect(java.util.stream.Collectors.toList()) : null)
                    .weeklyWorkingDays(responseDto.getWeeklyWorkingDays())
                    .startTime(responseDto.getStartTime())
                    .startTimeEnd(responseDto.getStartTimeEnd())
                    .endTime(responseDto.getEndTime())
                    .workHours(responseDto.getWorkHours())
                    .workMinutes(responseDto.getWorkMinutes())
                    .coreTimeStart(responseDto.getCoreTimeStart())
                    .coreTimeEnd(responseDto.getCoreTimeEnd())
                    .breakStartTime(responseDto.getBreakStartTime())
                    .breakEndTime(responseDto.getBreakEndTime())
                    .breakMinutes(responseDto.getBreakMinutes())
                    .avgWorkTime(responseDto.getAvgWorkTime())
                    .totalRequiredMinutes(responseDto.getTotalRequiredMinutes())
                    .holidays(responseDto.getHolidays())
                    .isHolidayFixed(responseDto.getIsHolidayFixed())
                    .isBreakFixed(responseDto.getIsBreakFixed())
                    .build();
        } catch (Exception e) {
            log.error("Error converting WorkPolicyResponseDto to WorkPolicyDto: {}", responseDto, e);
            throw new RuntimeException("근무정책 데이터 변환 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 동료 근무표 조회
     */
    public ColleagueScheduleResponseDto getColleagueSchedule(Long colleagueId, LocalDate startDate, LocalDate endDate) {
        try {
            // 1. 동료 정보 조회 (User Service에서)
            Map<String, Object> colleagueInfo = userServiceClient.getUserById(colleagueId);
            if (colleagueInfo == null) {
                log.error("Colleague not found: {}", colleagueId);
                return null;
            }
            
            // 2. 동료의 스케줄 조회
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateRange(colleagueId, "ACTIVE", startDate, endDate);
            log.info("Found {} schedules for colleague {} between {} and {}", 
                    schedules != null ? schedules.size() : 0, colleagueId, startDate, endDate);
            
            // 3. 일별 스케줄로 그룹화
            Map<LocalDate, List<Schedule>> schedulesByDate = schedules.stream()
                    .collect(Collectors.groupingBy(Schedule::getStartDate));
            
            // 4. 응답 DTO 생성
            List<ColleagueScheduleResponseDto.DailyScheduleDto> dailySchedules = new ArrayList<>();
            
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                List<Schedule> daySchedules = schedulesByDate.getOrDefault(currentDate, new ArrayList<>());
                
                List<ColleagueScheduleResponseDto.ScheduleEventDto> events = daySchedules.stream()
                        .map(this::convertToScheduleEventDto)
                        .collect(Collectors.toList());
                
                ColleagueScheduleResponseDto.DailyScheduleDto dailySchedule = ColleagueScheduleResponseDto.DailyScheduleDto.builder()
                        .date(currentDate)
                        .dayOfWeek(currentDate.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH))
                        .events(events)
                        .build();
                
                dailySchedules.add(dailySchedule);
                currentDate = currentDate.plusDays(1);
            }
            
            // Map에서 필요한 정보 추출 (안전한 null 체크 포함)
            String colleagueName = colleagueInfo.get("name") != null ? 
                    (String) colleagueInfo.get("name") : "알 수 없음";
            String colleaguePosition = "";
            String colleagueDepartment = colleagueInfo.get("departmentName") != null ? 
                    (String) colleagueInfo.get("departmentName") : "";
            String colleagueAvatar = colleagueInfo.get("profileImageUrl") != null ? 
                    (String) colleagueInfo.get("profileImageUrl") : null;
            
            // position 정보 추출
            if (colleagueInfo.get("position") != null) {
                Map<String, Object> position = (Map<String, Object>) colleagueInfo.get("position");
                if (position != null && position.get("name") != null) {
                    colleaguePosition = (String) position.get("name");
                }
            }
            
            log.info("Building response for colleague: name={}, position={}, department={}", 
                    colleagueName, colleaguePosition, colleagueDepartment);
            
            ColleagueScheduleResponseDto response = ColleagueScheduleResponseDto.builder()
                    .colleagueId(colleagueId)
                    .colleagueName(colleagueName)
                    .colleaguePosition(colleaguePosition)
                    .colleagueDepartment(colleagueDepartment)
                    .colleagueAvatar(colleagueAvatar)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dailySchedules(dailySchedules)
                    .build();
                    
            log.info("Successfully built colleague schedule response with {} daily schedules", 
                    dailySchedules.size());
            return response;
                    
        } catch (Exception e) {
            log.error("Error fetching colleague schedule for colleagueId: {} from {} to {}", colleagueId, startDate, endDate, e);
            throw new RuntimeException("Failed to fetch colleague schedule", e);
        }
    }
    
    /**
     * Schedule 엔티티를 ScheduleEventDto로 변환
     */
    private ColleagueScheduleResponseDto.ScheduleEventDto convertToScheduleEventDto(Schedule schedule) {
        return ColleagueScheduleResponseDto.ScheduleEventDto.builder()
                .scheduleId(schedule.getId())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .scheduleType(schedule.getScheduleType().toString())
                .build();
    }
} 