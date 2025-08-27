package com.hermes.workscheduleservice.example;

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
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * WorkScheduleService 사용 예제
 * 
 * 이 클래스는 work-schedule-service의 기능을 보여주는 예제입니다.
 * 실제 사용시에는 이 클래스를 삭제하고 Controller를 통해 API를 호출하세요.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkScheduleExample {
    
    private final WorkScheduleService workScheduleService;
    
    /**
     * 사용자 ID를 통해 해당 사용자의 근무 정책 정보를 조회하는 예제
     */
    public void getUserWorkPolicyExample(Long userId) {
        log.info("=== 사용자 근무 정책 조회 예제 ===");
        
        try {
            // 1. 사용자의 근무 정책 정보를 조회
            UserWorkPolicyDto userWorkPolicy = workScheduleService.getUserWorkPolicy(userId);
            
            if (userWorkPolicy == null) {
                log.warn("사용자 정보를 찾을 수 없습니다: {}", userId);
                return;
            }
            
            WorkPolicyDto workPolicy = userWorkPolicy.getWorkPolicy();
            
            log.info("근무 정책 정보:");
            log.info("  - WorkPolicy ID: {}", userWorkPolicy.getWorkPolicyId());
            
            if (workPolicy != null) {
                log.info("  - 정책 ID: {}", workPolicy.getId());
                log.info("  - 정책 이름: {}", workPolicy.getName());
                log.info("  - 근무 유형: {}", workPolicy.getType());
                log.info("  - 출근 시간: {}", workPolicy.getStartTime());
                log.info("  - 근무 시간: {}시간 {}분", workPolicy.getWorkHours(), workPolicy.getWorkMinutes());
                log.info("  - 휴게 시간: {}", workPolicy.getBreakStartTime());
                log.info("  - 근무 요일: {}", workPolicy.getWorkDays());
                
                // FullCalendar 이벤트 생성 예제
                createFullCalendarEvent(userId, workPolicy);
            } else {
                log.warn("사용자 {}에게 할당된 근무 정책이 없습니다.", userId);
            }
            
        } catch (Exception e) {
            log.error("사용자 근무 정책 조회 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 스케줄 생성 예제
     */
    public void createScheduleExample(Long userId) {
        log.info("=== 스케줄 생성 예제 ===");
        
        try {
            // 1. 기본 근무 스케줄 생성
            CreateScheduleRequestDto workSchedule = CreateScheduleRequestDto.builder()
                    .userId(userId)
                    .title("기본 근무")
                    .description("일반 근무 시간")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .scheduleType("WORK")
                    .color("#007bff")
                    .isAllDay(false)
                    .isRecurring(true)
                    .recurrencePattern("WEEKLY")
                    .recurrenceInterval(1)
                    .recurrenceDays(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"))
                    .priority(5)
                    .build();
            
            ScheduleResponseDto createdWorkSchedule = workScheduleService.createSchedule(workSchedule);
            log.info("근무 스케줄 생성 완료: {}", createdWorkSchedule.getId());
            
            // 2. 회의 스케줄 생성
            CreateScheduleRequestDto meetingSchedule = CreateScheduleRequestDto.builder()
                    .userId(userId)
                    .title("팀 회의")
                    .description("주간 팀 회의")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .scheduleType("MEETING")
                    .color("#28a745")
                    .isAllDay(false)
                    .isRecurring(true)
                    .recurrencePattern("WEEKLY")
                    .recurrenceInterval(1)
                    .recurrenceDays(Arrays.asList("TUESDAY"))
                    .location("회의실 A")
                    .attendees(Arrays.asList("team1@company.com", "team2@company.com"))
                    .priority(7)
                    .build();
            
            ScheduleResponseDto createdMeetingSchedule = workScheduleService.createSchedule(meetingSchedule);
            log.info("회의 스케줄 생성 완료: {}", createdMeetingSchedule.getId());
            
            // 3. 휴가 스케줄 생성
            CreateScheduleRequestDto vacationSchedule = CreateScheduleRequestDto.builder()
                    .userId(userId)
                    .title("연차 휴가")
                    .description("연차 사용")
                    .startDate(LocalDate.now().plusDays(7))
                    .endDate(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(0, 0))
                    .endTime(LocalTime.of(23, 59))
                    .scheduleType("VACATION")
                    .color("#dc3545")
                    .isAllDay(true)
                    .isRecurring(false)
                    .priority(8)
                    .build();
            
            ScheduleResponseDto createdVacationSchedule = workScheduleService.createSchedule(vacationSchedule);
            log.info("휴가 스케줄 생성 완료: {}", createdVacationSchedule.getId());
            
            // 4. 생성된 스케줄 조회
            List<ScheduleResponseDto> userSchedules = workScheduleService.getUserSchedules(userId);
            log.info("사용자 {}의 총 스케줄 개수: {}", userId, userSchedules.size());
            
            for (ScheduleResponseDto schedule : userSchedules) {
                log.info("스케줄: {} - {} ({} ~ {})", 
                    schedule.getId(), 
                    schedule.getTitle(), 
                    schedule.getStartDate(), 
                    schedule.getEndDate());
            }
            
        } catch (Exception e) {
            log.error("스케줄 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 스케줄 수정 예제
     */
    public void updateScheduleExample(Long userId, Long scheduleId) {
        log.info("=== 스케줄 수정 예제 ===");
        
        try {
            // 스케줄 수정
            UpdateScheduleRequestDto updateRequest = UpdateScheduleRequestDto.builder()
                    .title("수정된 팀 회의")
                    .description("수정된 주간 팀 회의")
                    .startDate(LocalDate.now().plusDays(2))
                    .endDate(LocalDate.now().plusDays(2))
                    .startTime(LocalTime.of(15, 0))
                    .endTime(LocalTime.of(16, 30))
                    .scheduleType("MEETING")
                    .color("#ffc107")
                    .isAllDay(false)
                    .isRecurring(true)
                    .recurrencePattern("WEEKLY")
                    .recurrenceInterval(1)
                    .recurrenceDays(Arrays.asList("WEDNESDAY"))
                    .location("회의실 B")
                    .attendees(Arrays.asList("team1@company.com", "team2@company.com", "team3@company.com"))
                    .priority(8)
                    .notes("회의 시간이 변경되었습니다.")
                    .build();
            
            ScheduleResponseDto updatedSchedule = workScheduleService.updateSchedule(userId, scheduleId, updateRequest);
            log.info("스케줄 수정 완료: {} - {}", updatedSchedule.getId(), updatedSchedule.getTitle());
            
        } catch (Exception e) {
            log.error("스케줄 수정 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 근무 시간 조정 예제
     */
    public void workTimeAdjustmentExample(Long userId) {
        log.info("=== 근무 시간 조정 예제 ===");
        
        try {
            // 1. 근무 시간 연장 요청
            AdjustWorkTimeRequestDto extendRequest = AdjustWorkTimeRequestDto.builder()
                    .userId(userId)
                    .adjustDate(LocalDate.now().plusDays(3))
                    .adjustType("EXTEND")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(20, 0))
                    .reason("프로젝트 마감일로 인한 업무량 증가")
                    .description("중요 프로젝트 마감을 위해 2시간 연장 근무 필요")
                    .build();
            
            WorkTimeAdjustment extendAdjustment = workScheduleService.createWorkTimeAdjustment(extendRequest);
            log.info("근무 시간 연장 요청 생성 완료: {}", extendAdjustment.getId());
            
            // 2. 근무 시간 단축 요청
            AdjustWorkTimeRequestDto reduceRequest = AdjustWorkTimeRequestDto.builder()
                    .userId(userId)
                    .adjustDate(LocalDate.now().plusDays(5))
                    .adjustType("REDUCE")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(16, 0))
                    .reason("개인 사정으로 인한 단축 근무")
                    .description("의료진료를 위해 2시간 단축 근무 필요")
                    .build();
            
            WorkTimeAdjustment reduceAdjustment = workScheduleService.createWorkTimeAdjustment(reduceRequest);
            log.info("근무 시간 단축 요청 생성 완료: {}", reduceAdjustment.getId());
            
            // 3. 유연근무 요청
            AdjustWorkTimeRequestDto flexibleRequest = AdjustWorkTimeRequestDto.builder()
                    .userId(userId)
                    .adjustDate(LocalDate.now().plusDays(7))
                    .adjustType("FLEXIBLE")
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(19, 0))
                    .reason("개인 생산성 향상을 위한 유연근무")
                    .description("오후 시간대에 집중력이 높아 유연근무 희망")
                    .build();
            
            WorkTimeAdjustment flexibleAdjustment = workScheduleService.createWorkTimeAdjustment(flexibleRequest);
            log.info("유연근무 요청 생성 완료: {}", flexibleAdjustment.getId());
            
            // 4. 사용자의 근무 시간 조정 요청 조회
            List<WorkTimeAdjustment> userAdjustments = workScheduleService.getUserWorkTimeAdjustments(userId);
            log.info("사용자 {}의 근무 시간 조정 요청 개수: {}", userId, userAdjustments.size());
            
            for (WorkTimeAdjustment adjustment : userAdjustments) {
                log.info("조정 요청: {} - {} ({} ~ {}) - 상태: {}", 
                    adjustment.getId(), 
                    adjustment.getAdjustType(), 
                    adjustment.getStartTime(), 
                    adjustment.getEndTime(),
                    adjustment.getStatus());
            }
            
            // 5. 승인 대기 중인 요청 조회
            List<WorkTimeAdjustment> pendingAdjustments = workScheduleService.getPendingWorkTimeAdjustments();
            log.info("승인 대기 중인 근무 시간 조정 요청 개수: {}", pendingAdjustments.size());
            
            // 6. 근무 시간 조정 승인 예제 (관리자 권한)
            if (!pendingAdjustments.isEmpty()) {
                WorkTimeAdjustment firstPending = pendingAdjustments.get(0);
                WorkTimeAdjustment approvedAdjustment = workScheduleService.processWorkTimeAdjustment(
                    firstPending.getId(), 
                    "admin@company.com", 
                    true, 
                    "승인되었습니다."
                );
                log.info("근무 시간 조정 승인 완료: {} - {}", 
                    approvedAdjustment.getId(), 
                    approvedAdjustment.getStatus());
            }
            
            // 7. 근무 시간 조정 삭제 예제
            if (!userAdjustments.isEmpty()) {
                WorkTimeAdjustment adjustmentToDelete = userAdjustments.stream()
                        .filter(adj -> "PENDING".equals(adj.getStatus()))
                        .findFirst()
                        .orElse(null);
                
                if (adjustmentToDelete != null) {
                    workScheduleService.deleteWorkTimeAdjustment(userId, adjustmentToDelete.getId());
                    log.info("근무 시간 조정 삭제 완료: {}", adjustmentToDelete.getId());
                    
                    // 삭제 후 상태 확인
                    List<WorkTimeAdjustment> updatedAdjustments = workScheduleService.getUserWorkTimeAdjustments(userId);
                    log.info("삭제 후 사용자 {}의 근무 시간 조정 요청 개수: {}", userId, updatedAdjustments.size());
                }
            }
            
        } catch (Exception e) {
            log.error("근무 시간 조정 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 근무 시간 조정 삭제 예제
     */
    public void deleteWorkTimeAdjustmentExample(Long userId) {
        log.info("=== 근무 시간 조정 삭제 예제 ===");
        
        try {
            // 1. 삭제할 근무 시간 조정 요청 생성
            AdjustWorkTimeRequestDto deleteRequest = AdjustWorkTimeRequestDto.builder()
                    .userId(userId)
                    .adjustDate(LocalDate.now().plusDays(10))
                    .adjustType("EXTEND")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(19, 0))
                    .reason("삭제 테스트용 요청")
                    .description("이 요청은 삭제 테스트를 위해 생성되었습니다.")
                    .build();
            
            WorkTimeAdjustment adjustmentToDelete = workScheduleService.createWorkTimeAdjustment(deleteRequest);
            log.info("삭제 테스트용 근무 시간 조정 요청 생성 완료: {}", adjustmentToDelete.getId());
            
            // 2. 삭제 전 상태 확인
            WorkTimeAdjustment beforeDelete = workScheduleService.getWorkTimeAdjustmentById(userId, adjustmentToDelete.getId());
            log.info("삭제 전 상태: {} - {}", beforeDelete.getId(), beforeDelete.getStatus());
            
            // 3. 근무 시간 조정 삭제
            workScheduleService.deleteWorkTimeAdjustment(userId, adjustmentToDelete.getId());
            log.info("근무 시간 조정 삭제 완료: {}", adjustmentToDelete.getId());
            
            // 4. 삭제 후 상태 확인 (CANCELLED 상태로 변경됨)
            WorkTimeAdjustment afterDelete = workScheduleService.getWorkTimeAdjustmentById(userId, adjustmentToDelete.getId());
            log.info("삭제 후 상태: {} - {}", afterDelete.getId(), afterDelete.getStatus());
            
            // 5. 전체 조정 요청 목록 확인
            List<WorkTimeAdjustment> allAdjustments = workScheduleService.getUserWorkTimeAdjustments(userId);
            log.info("전체 근무 시간 조정 요청 개수: {}", allAdjustments.size());
            
            for (WorkTimeAdjustment adjustment : allAdjustments) {
                log.info("조정 요청: {} - {} - 상태: {}", 
                    adjustment.getId(), 
                    adjustment.getAdjustType(), 
                    adjustment.getStatus());
            }
            
        } catch (Exception e) {
            log.error("근무 시간 조정 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 고정 스케줄 생성 예제
     */
    public void createFixedSchedulesExample(Long userId) {
        log.info("=== 고정 스케줄 생성 예제 ===");
        
        try {
            // 1. 이번 달 고정 스케줄 생성
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            List<ScheduleResponseDto> fixedSchedules = workScheduleService.createFixedSchedulesFromWorkPolicy(userId, startDate, endDate);
            log.info("고정 스케줄 생성 완료: {}개", fixedSchedules.size());
            
            // 2. 생성된 고정 스케줄 분류
            long workSchedules = fixedSchedules.stream()
                    .filter(schedule -> "WORK".equals(schedule.getScheduleType()))
                    .count();
            
            long breakSchedules = fixedSchedules.stream()
                    .filter(schedule -> "BREAK".equals(schedule.getScheduleType()))
                    .count();
            
            long holidaySchedules = fixedSchedules.stream()
                    .filter(schedule -> "HOLIDAY".equals(schedule.getScheduleType()))
                    .count();
            
            log.info("근무일 스케줄: {}개", workSchedules);
            log.info("휴식 시간 스케줄: {}개", breakSchedules);
            log.info("휴일 스케줄: {}개", holidaySchedules);
            
            // 3. 고정 스케줄 상세 정보 출력
            for (ScheduleResponseDto schedule : fixedSchedules) {
                log.info("고정 스케줄: {} - {} ({} ~ {}) - 편집가능: {}", 
                    schedule.getId(), 
                    schedule.getTitle(), 
                    schedule.getStartDate(), 
                    schedule.getEndDate(),
                    schedule.getIsEditable());
            }
            
            // 4. 고정 스케줄 수정 시도 (실패해야 함)
            if (!fixedSchedules.isEmpty()) {
                ScheduleResponseDto fixedSchedule = fixedSchedules.stream()
                        .filter(schedule -> !schedule.getIsEditable())
                        .findFirst()
                        .orElse(null);
                
                if (fixedSchedule != null) {
                    try {
                        UpdateScheduleRequestDto updateRequest = UpdateScheduleRequestDto.builder()
                                .title("수정 시도")
                                .description("고정 스케줄 수정 시도")
                                .startDate(fixedSchedule.getStartDate())
                                .endDate(fixedSchedule.getEndDate())
                                .startTime(fixedSchedule.getStartTime())
                                .endTime(fixedSchedule.getEndTime())
                                .scheduleType(fixedSchedule.getScheduleType())
                                .build();
                        
                        workScheduleService.updateSchedule(userId, fixedSchedule.getId(), updateRequest);
                        log.warn("고정 스케줄 수정이 허용되었습니다. (예상과 다름)");
                    } catch (Exception e) {
                        log.info("고정 스케줄 수정이 차단되었습니다: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("고정 스케줄 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * FullCalendar 이벤트 생성 예제
     */
    private void createFullCalendarEvent(Long userId, WorkPolicyDto workPolicy) {
        log.info("=== FullCalendar 이벤트 생성 예제 ===");
        
        // 기본 근무 시간 이벤트
        String eventJson = String.format("""
            {
                "id": "policy_%d",
                "title": "사용자 %d의 기본 근무 시간",
                "start": "2024-01-15T%s",
                "end": "2024-01-15T%s",
                "backgroundColor": "#6c757d",
                "borderColor": "#6c757d",
                "extendedProps": {
                    "type": "policy",
                    "userId": %d,
                    "workPolicyId": %d,
                    "workHours": %d,
                    "workMinutes": %d
                }
            }
            """,
            workPolicy.getId(),
            userId,
            workPolicy.getStartTime(),
            workPolicy.getStartTime().plusHours(workPolicy.getWorkHours()).plusMinutes(workPolicy.getWorkMinutes()),
            userId,
            workPolicy.getId(),
            workPolicy.getWorkHours(),
            workPolicy.getWorkMinutes()
        );
        
        log.info("FullCalendar 이벤트 JSON:");
        log.info(eventJson);
    }
    
    /**
     * 근무 정책 정보만 조회하는 예제
     */
    public void getUserWorkPolicyOnlyExample(Long userId) {
        log.info("=== 근무 정책 정보만 조회 예제 ===");
        
        try {
            WorkPolicyDto workPolicy = workScheduleService.getUserWorkPolicyOnly(userId);
            
            if (workPolicy != null) {
                log.info("근무 정책 정보:");
                log.info("  - 정책 ID: {}", workPolicy.getId());
                log.info("  - 정책 이름: {}", workPolicy.getName());
                log.info("  - 근무 유형: {}", workPolicy.getType());
            } else {
                log.warn("사용자 {}의 근무 정책을 찾을 수 없습니다.", userId);
            }
            
        } catch (Exception e) {
            log.error("근무 정책 조회 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 