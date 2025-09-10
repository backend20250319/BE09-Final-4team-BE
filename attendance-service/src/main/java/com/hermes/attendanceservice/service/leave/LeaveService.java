package com.hermes.attendanceservice.service.leave;

import com.hermes.attendanceservice.entity.leave.LeaveRequest;
import com.hermes.attendanceservice.entity.leave.LeaveType;
import com.hermes.attendanceservice.dto.leave.CreateLeaveRequestDto;
import com.hermes.attendanceservice.dto.leave.LeaveRequestResponseDto;
import com.hermes.attendanceservice.repository.leave.LeaveRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LeaveService {
    
    private static final double WORK_HOURS_PER_DAY = 8.0;
    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);
    
    private final LeaveRepository leaveRequestRepository;
    private final EmployeeLeaveBalanceService employeeLeaveBalanceService;
    
    /**
     * 휴가 신청을 생성합니다.
     * @param createDto 휴가 신청 생성 DTO
     * @return 생성된 휴가 신청 응답 DTO
     */
    public LeaveRequestResponseDto createLeaveRequest(CreateLeaveRequestDto createDto) {
        // 1. 휴가 신청 검증
        validateLeaveRequest(createDto);
        
        // 2. 총 휴가 시간/일수 계산
        double totalHours = calculateTotalHours(createDto);
        double totalDays = totalHours / WORK_HOURS_PER_DAY;
        int requestedDays = (int) Math.ceil(totalDays); // 올림 처리
        
        // 2-1. 주말만 선택한 경우 예외 처리
        if (totalHours == 0 || requestedDays == 0) {
            throw new IllegalArgumentException("선택한 날짜에 근무일이 없습니다. 평일을 선택해주세요.");
        }
        
        // 3. 연차 잔액 확인 및 차감
        Integer remainingLeave = employeeLeaveBalanceService.getRemainingLeave(createDto.getEmployeeId(), createDto.getLeaveType());
        if (remainingLeave < requestedDays) {
            throw new IllegalArgumentException("잔여 연차가 부족합니다. 잔여: " + remainingLeave + "일, 요청: " + requestedDays + "일");
        }
        
        // 4. LeaveRequest 엔티티 생성 (엔티티의 정적 팩토리 메서드 사용)
        LeaveRequest leaveRequest = LeaveRequest.createFromDto(createDto, totalDays);
        
        // 5. 저장
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        // 6. 연차 잔액 차감 (신청 즉시 차감, 승인/반려 시 복구/확정)
        try {
            employeeLeaveBalanceService.useLeave(createDto.getEmployeeId(), createDto.getLeaveType(), requestedDays);
        } catch (Exception e) {
            // 연차 차감 실패 시 휴가 신청도 롤백
            leaveRequestRepository.delete(savedRequest);
            throw new RuntimeException("연차 차감 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        // 7. 응답 DTO 변환
        return convertToResponseDto(savedRequest);
    }
    
    /**
     * 휴가 신청을 수정합니다.(기존 삭제 후 새로 생성 방식)
     * @param requestId 기존 휴가 신청 ID
     * @param createDto 새로운 휴가 신청 내용
     * @return 새로 생성된 휴가 신청 응답 DTO
     */
    public LeaveRequestResponseDto modifyLeaveRequest(Long requestId, CreateLeaveRequestDto createDto) {
        // 1. 기존 휴가 신청 조회 및 검증
        LeaveRequest existingRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("휴가 신청을 찾을 수 없습니다"));
        
        // 2. 수정 가능한 상태인지 확인
        if (existingRequest.getStatus() != LeaveRequest.RequestStatus.REQUESTED) {
            throw new RuntimeException("수정할 수 없는 상태입니다");
        }
        
        // 3. 기존 휴가 신청 삭제
        leaveRequestRepository.delete(existingRequest);
        
        // 4. 새로운 휴가 신청 생성
        return createLeaveRequest(createDto);
    }
    
    /**
     * 휴가 신청을 조회합니다.
     * @param requestId 휴가 신청 ID
     * @return 휴가 신청 응답 DTO
     */
    @Transactional(readOnly = true)
    public LeaveRequestResponseDto getLeaveRequest(Long requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElse(null);
        
        if (leaveRequest == null) {
            return null;
        }
        
        return convertToResponseDto(leaveRequest);
    }
    
    /**
     * 휴가 신청 검증
     */
    private void validateLeaveRequest(CreateLeaveRequestDto createDto) {
        // 시작일이 종료일보다 늦으면 안됨
        if (createDto.getStartDate().isAfter(createDto.getEndDate())) {
            throw new RuntimeException("시작일은 종료일보다 늦을 수 없습니다.");
        }
        
        // 시작일이 오늘보다 이전이면 안됨
        if (createDto.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("시작일은 오늘 이후여야 합니다.");
        }
        
        // 같은 기간에 다른 휴가 신청이 있는지 확인
        List<LeaveRequest> existingRequests = leaveRequestRepository.findByStatusAndDateRange(
                LeaveRequest.RequestStatus.REQUESTED,
                createDto.getStartDate(),
                createDto.getEndDate()
        );
        
        boolean hasConflict = existingRequests.stream()
                .anyMatch(request -> request.getEmployeeId().equals(createDto.getEmployeeId()));
        
        if (hasConflict) {
            throw new RuntimeException("해당 기간에 이미 휴가 신청이 있습니다.");
        }
    }
    
    /**
     * 총 휴가 시간 계산 (주말 제외)
     */
    private double calculateTotalHours(CreateLeaveRequestDto createDto) {
        long daysBetween = ChronoUnit.DAYS.between(createDto.getStartDate(), createDto.getEndDate()) + 1;
        
        if (daysBetween == 1) {
            // 하루 휴가인 경우 - 주말 체크
            LocalDate singleDate = createDto.getStartDate();
            if (isWeekend(singleDate)) {
                return 0; // 주말은 휴가 시간으로 계산하지 않음
            }
            
            // 시간 계산
            if (createDto.getStartTime() != null && createDto.getEndTime() != null) {
                Duration duration = Duration.between(createDto.getStartTime(), createDto.getEndTime());
                return duration.toHours() + (duration.toMinutes() % 60) / 60.0;
            } else {
                return WORK_HOURS_PER_DAY;
            }
        } else {
            // 여러 날 휴가인 경우 - 주말 제외하고 계산
            double totalHours = 0;
            
            for (int i = 0; i < daysBetween; i++) {
                LocalDate currentDate = createDto.getStartDate().plusDays(i);
                
                // 주말은 건너뛰기
                if (isWeekend(currentDate)) {
                    continue;
                }
                
                if (i == 0 && createDto.getStartTime() != null) {
                    // 첫날 (주말이 아닌 경우만)
                    Duration duration = Duration.between(createDto.getStartTime(), WORK_END_TIME);
                    totalHours += duration.toHours() + (duration.toMinutes() % 60) / 60.0;
                } else if (i == daysBetween - 1 && createDto.getEndTime() != null) {
                    // 마지막날 (주말이 아닌 경우만)
                    Duration duration = Duration.between(WORK_START_TIME, createDto.getEndTime());
                    totalHours += duration.toHours() + (duration.toMinutes() % 60) / 60.0;
                } else {
                    // 중간 날들 (주말이 아닌 경우만)
                    totalHours += WORK_HOURS_PER_DAY;
                }
            }
            
            return totalHours;
        }
    }
    
    /**
     * 주말(토요일, 일요일) 여부 확인
     */
    private boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
    }
    
    /**
     * DTO 변환
     */
    private LeaveRequestResponseDto convertToResponseDto(LeaveRequest leaveRequest) {
        return LeaveRequestResponseDto.builder()
                .requestId(leaveRequest.getRequestId())
                .employeeId(leaveRequest.getEmployeeId())
                .leaveType(leaveRequest.getLeaveType())
                .leaveTypeName(leaveRequest.getLeaveType().getName())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .startTime(leaveRequest.getStartTime())
                .endTime(leaveRequest.getEndTime())
                .totalDays(leaveRequest.getTotalDays())
                .totalHours(leaveRequest.getTotalDays() * WORK_HOURS_PER_DAY)
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus().name())
                .statusName(leaveRequest.getStatus().name())
                .approverId(leaveRequest.getApproverId())
                .requestedAt(leaveRequest.getRequestedAt())
                .approvedAt(leaveRequest.getApprovedAt())
                .build();
    }
} 