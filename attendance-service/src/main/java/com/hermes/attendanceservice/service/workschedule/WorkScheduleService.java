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
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        
        // 스케줄이 없으면 기본 근무 정책 사용
        try {
            UserWorkPolicyDto userPolicy = getUserWorkPolicy(userId, null);
            if (userPolicy.getWorkPolicy() != null) {
                WorkPolicyDto workPolicy = userPolicy.getWorkPolicy();
                LocalTime startTime = workPolicy.getStartTime();
                
                // 근무 시간을 계산하여 종료 시간 도출
                LocalTime endTime;
                if (workPolicy.getWorkHours() != null && workPolicy.getWorkMinutes() != null) {
                    int totalMinutes = workPolicy.getWorkHours() * 60 + workPolicy.getWorkMinutes();
                    endTime = startTime.plusMinutes(totalMinutes);
                } else if (workPolicy.getWorkHours() != null) {
                    endTime = startTime.plusHours(workPolicy.getWorkHours());
                } else {
                    // 기본값: 8시간 근무
                    endTime = startTime.plusHours(8);
                }
                
                return WorkTimeInfoDto.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
            }
        } catch (Exception e) {
            log.warn("Failed to get user work policy for userId: {}, date: {}", userId, date, e);
        }
        
        // 기본값
        return WorkTimeInfoDto.builder()
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(18, 0))
            .build();
    }
    
    /**
     * 사용자 ID를 통해 해당 사용자의 근무 정책 정보를 조회
     */
    public UserWorkPolicyDto getUserWorkPolicy(Long userId, String authorization) {
        try {
            // 1. User Service에서 사용자 정보 조회
            Map<String, Object> userResponse = userServiceClient.getUserById(userId);
            
            if (userResponse == null) {
                log.warn("User not found with id: {}", userId);
                return null;
            }
            
            // 2. workPolicyId 추출
            Long workPolicyId = userResponse.get("workPolicyId") != null ? 
                Long.valueOf(userResponse.get("workPolicyId").toString()) : null;
            
            if (workPolicyId == null) {
                log.warn("User {} has no work policy assigned", userId);
                return UserWorkPolicyDto.builder()
                        .workPolicyId(null)
                        .workPolicy(null)
                        .build();
            }
            
            // 3. 근무 정책 정보 조회 (WorkPolicyService 사용)
            WorkPolicyResponseDto workPolicyResponse = workPolicyService.getWorkPolicyById(workPolicyId);
            
            // 4. WorkPolicyResponseDto를 WorkPolicyDto로 변환
            WorkPolicyDto workPolicy = null;
            if (workPolicyResponse != null) {
                workPolicy = convertToWorkPolicyDto(workPolicyResponse);
            }
            
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
            UserWorkPolicyDto userWorkPolicy = getUserWorkPolicy(userId, null);
            if (userWorkPolicy == null || userWorkPolicy.getWorkPolicy() == null) {
                throw new RuntimeException("User has no work policy assigned");
            }
            
            WorkPolicyDto workPolicy = userWorkPolicy.getWorkPolicy();
            
            // 2. 기존 고정 스케줄 삭제 (해당 기간)
            deleteExistingFixedSchedules(userId, startDate, endDate);
            
            // 3. 근무일 스케줄 생성
            List<Schedule> workSchedules = createWorkDaySchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
            
            // 4. 휴식 시간 스케줄 생성
            List<Schedule> breakSchedules = createBreakTimeSchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
            
            // 5. 휴일 스케줄 생성
            List<Schedule> holidaySchedules = createHolidaySchedules(userId, workPolicy, userWorkPolicy.getWorkPolicyId(), startDate, endDate);
            
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
     * WorkPolicy 정보를 기반으로 Workschedule에 반영하여 저장
     * 근무시간, 휴게시간, 출근시간, 퇴근시간, 코어시간, 시차 근무 출근 가능 시간 등을 스케줄로 생성
     */
    @Transactional
    public List<ScheduleResponseDto> applyWorkPolicyToSchedule(Long userId, String authorization, LocalDate startDate, LocalDate endDate) {
        try {
            // 1. 사용자의 WorkPolicy 정보 조회
            UserWorkPolicyDto userWorkPolicy = getUserWorkPolicy(userId, authorization);
            
            if (userWorkPolicy == null || userWorkPolicy.getWorkPolicy() == null) {
                throw new RuntimeException("User has no work policy assigned");
            }
            
            WorkPolicyDto workPolicy = userWorkPolicy.getWorkPolicy();
            Long workPolicyId = userWorkPolicy.getWorkPolicyId();
            
            log.info("Applying work policy to schedule for userId: {}, workPolicyId: {}, period: {} to {}", 
                    userId, workPolicyId, startDate, endDate);
            
            // 2. 기존 고정 스케줄 삭제 (해당 기간)
            deleteExistingFixedSchedules(userId, startDate, endDate);
            
            // 3. 근무일 스케줄 생성 (출근시간, 퇴근시간, 근무시간)
            List<Schedule> workSchedules = createWorkDaySchedules(userId, workPolicy, workPolicyId, startDate, endDate);
            
            // 4. 휴식 시간 스케줄 생성 (휴게시간)
            List<Schedule> breakSchedules = createBreakTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
            
            // 5. 코어 타임 스케줄 생성 (선택 근무인 경우)
            List<Schedule> coreTimeSchedules = createCoreTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
            
            // 6. 시차 근무 출근 가능 시간대 스케줄 생성 (시차 근무인 경우)
            List<Schedule> flexibleSchedules = createFlexibleWorkTimeSchedules(userId, workPolicy, workPolicyId, startDate, endDate);
            
            // 7. 휴일 스케줄 생성 (휴일)
            List<Schedule> holidaySchedules = createHolidaySchedules(userId, workPolicy, workPolicyId, startDate, endDate);
            
            // 8. 모든 스케줄 저장
            List<Schedule> allSchedules = new ArrayList<>();
            allSchedules.addAll(workSchedules);
            allSchedules.addAll(breakSchedules);
            allSchedules.addAll(coreTimeSchedules);
            allSchedules.addAll(flexibleSchedules);
            allSchedules.addAll(holidaySchedules);
            
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
     * 근무일 스케줄 생성 (출근시간, 퇴근시간, 근무시간 반영)
     */
    private List<Schedule> createWorkDaySchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> workSchedules = new ArrayList<>();
        
        if (workPolicy.getWorkDays() == null || workPolicy.getWorkDays().isEmpty()) {
            return workSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays().contains(dayOfWeek)) {
                // 출근 시간 계산
                LocalTime startTime = workPolicy.getStartTime();
                if (startTime == null) {
                    startTime = LocalTime.of(9, 0); // 기본 출근시간 9시
                }
                
                // 퇴근 시간 계산 (근무시간 반영)
                LocalTime endTime;
                if (workPolicy.getWorkHours() != null && workPolicy.getWorkMinutes() != null) {
                    endTime = startTime.plusHours(workPolicy.getWorkHours()).plusMinutes(workPolicy.getWorkMinutes());
                } else {
                    endTime = startTime.plusHours(8); // 기본 8시간 근무
                }
                
                Schedule workSchedule = Schedule.builder()
                        .userId(userId)
                        .title("근무일")
                        .description(String.format("출근: %s, 퇴근: %s, 근무시간: %d시간 %d분", 
                                startTime, endTime, 
                                workPolicy.getWorkHours() != null ? workPolicy.getWorkHours() : 8,
                                workPolicy.getWorkMinutes() != null ? workPolicy.getWorkMinutes() : 0))
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
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return workSchedules;
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
                // 휴게 종료 시간 계산
                LocalTime breakEndTime;
                if (workPolicy.getBreakMinutes() != null) {
                    breakEndTime = workPolicy.getBreakStartTime().plusMinutes(workPolicy.getBreakMinutes());
                } else {
                    breakEndTime = workPolicy.getBreakStartTime().plusHours(1); // 기본 1시간 휴게
                }
                
                Schedule breakSchedule = Schedule.builder()
                        .userId(userId)
                        .title("휴식 시간")
                        .description(String.format("휴게시간: %s ~ %s (%d분)", 
                                workPolicy.getBreakStartTime(), breakEndTime,
                                workPolicy.getBreakMinutes() != null ? workPolicy.getBreakMinutes() : 60))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getBreakStartTime())
                        .endTime(breakEndTime)
                        .scheduleType(ScheduleType.WORK) // BREAK는 WORK 타입으로 처리
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
     * 코어 타임 스케줄 생성 (코어시간 반영)
     * 시차 근무의 경우 출근 가능 시간대가 설정되어 있을 수 있음
     */
    private List<Schedule> createCoreTimeSchedules(Long userId, WorkPolicyDto workPolicy, Long workPolicyId, LocalDate startDate, LocalDate endDate) {
        List<Schedule> coreTimeSchedules = new ArrayList<>();
        
        // 코어 타임은 선택 근무에서만 적용
        // 시차 근무의 경우 출근 가능 시간대(startTimeEnd)가 설정되어 있을 수 있음
        if (workPolicy.getCoreTimeStart() == null || workPolicy.getCoreTimeEnd() == null) {
            return coreTimeSchedules;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
            
            if (workPolicy.getWorkDays() != null && workPolicy.getWorkDays().contains(dayOfWeek)) {
                Schedule coreTimeSchedule = Schedule.builder()
                        .userId(userId)
                        .title("코어 타임")
                        .description(String.format("필수 근무시간: %s ~ %s", 
                                workPolicy.getCoreTimeStart(), workPolicy.getCoreTimeEnd()))
                        .startDate(currentDate)
                        .endDate(currentDate)
                        .startTime(workPolicy.getCoreTimeStart())
                        .endTime(workPolicy.getCoreTimeEnd())
                        .scheduleType(ScheduleType.WORK) // CORE_TIME은 WORK로 처리
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
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
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
            UserWorkPolicyDto userWorkPolicy = getUserWorkPolicy(userId, authorization);
            
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
        return WorkPolicyDto.builder()
                .id(responseDto.getId())
                .name(responseDto.getName())
                .type(responseDto.getType().toString())
                .workCycle(responseDto.getWorkCycle() != null ? responseDto.getWorkCycle().toString() : null)
                .startDayOfWeek(responseDto.getStartDayOfWeek() != null ? responseDto.getStartDayOfWeek().toString() : null)
                .workCycleStartDay(responseDto.getWorkCycleStartDay())
                .workDays(responseDto.getWorkDays() != null ? 
                    responseDto.getWorkDays().stream()
                        .map(StartDayOfWeek::toString)
                        .collect(java.util.stream.Collectors.toList()) : null)
                .holidayDays(responseDto.getHolidayDays() != null ? 
                    responseDto.getHolidayDays().stream()
                        .map(StartDayOfWeek::toString)
                        .collect(java.util.stream.Collectors.toList()) : null)
                .weeklyWorkingDays(responseDto.getWeeklyWorkingDays())
                .startTime(responseDto.getStartTime())
                .startTimeEnd(responseDto.getStartTimeEnd())
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
    }
    
    /**
     * 동료 근무표 조회
     */
    public ColleagueScheduleResponseDto getColleagueSchedule(Long colleagueId, LocalDate startDate, LocalDate endDate) {
        try {
            // 1. 동료 정보 조회 (User Service에서)
            Map<String, Object> colleagueInfo = userServiceClient.getUserById(colleagueId, null);
            if (colleagueInfo == null) {
                log.error("Colleague not found: {}", colleagueId);
                return null;
            }
            
            // 2. 동료의 스케줄 조회
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateRange(colleagueId, "ACTIVE", startDate, endDate);
            
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
            
            // Map에서 필요한 정보 추출
            String colleagueName = (String) colleagueInfo.get("name");
            String colleaguePosition = "";
            String colleagueDepartment = "";
            String colleagueAvatar = (String) colleagueInfo.get("profileImageUrl");
            
            // position 정보 추출
            if (colleagueInfo.get("position") != null) {
                Map<String, Object> position = (Map<String, Object>) colleagueInfo.get("position");
                colleaguePosition = (String) position.get("name");
            }
            
            return ColleagueScheduleResponseDto.builder()
                    .colleagueId(colleagueId)
                    .colleagueName(colleagueName)
                    .colleaguePosition(colleaguePosition)
                    .colleagueDepartment(colleagueDepartment)
                    .colleagueAvatar(colleagueAvatar)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dailySchedules(dailySchedules)
                    .build();
                    
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