package com.hermes.leaveservice.service;

import com.hermes.leaveservice.entity.LeaveRequest;
import com.hermes.leaveservice.entity.LeaveType;
import com.hermes.leaveservice.dto.CreateLeaveRequestDto;
import com.hermes.leaveservice.dto.LeaveRequestResponseDto;
import com.hermes.userservice.entity.User;
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
public class LeaveService {
    
    private static final double WORK_HOURS_PER_DAY = 8.0;
    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);
    
    // TODO: Repository 주입 필요
    // private final LeaveRequestRepository leaveRequestRepository;
    // private final UserRepository userRepository;
    
    /**
     * 휴가 신청을 생성합니다
     * @param createDto 휴가 신청 생성 DTO
     * @return 생성된 휴가 신청 응답 DTO
     */
    public LeaveRequestResponseDto createLeaveRequest(CreateLeaveRequestDto createDto) {
        // 1. 휴가 신청 검증
        validateLeaveRequest(createDto);
        
        // 2. 총 휴가 시간/일수 계산
        double totalHours = calculateTotalHours(createDto);
        double totalDays = totalHours / WORK_HOURS_PER_DAY;
        
        // 3. LeaveRequest 엔티티 생성
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(getUserById(createDto.getEmployeeId()))
                .leaveType(createDto.getLeaveType())
                .startDate(createDto.getStartDate())
                .endDate(createDto.getEndDate())
                .startTime(createDto.getStartTime())
                .endTime(createDto.getEndTime())
                .totalDays(totalDays)
                .reason(createDto.getReason())
                .status(LeaveRequest.RequestStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();
        
        // 4. 저장
        // LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        // 5. 응답 DTO 변환
        return convertToResponseDto(leaveRequest);
    }
    
    /**
     * 휴가 신청을 수정합니다 (삭제 후 재생성 방식)
     * @param requestId 기존 휴가 신청 ID
     * @param createDto 새로운 휴가 신청 데이터
     * @return 새로 생성된 휴가 신청 응답 DTO
     */
    public LeaveRequestResponseDto modifyLeaveRequest(Long requestId, CreateLeaveRequestDto createDto) {
        // 1. 기존 휴가 신청 조회 및 검증
        // LeaveRequest existingRequest = leaveRequestRepository.findById(requestId)
        //         .orElseThrow(() -> new RuntimeException("휴가 신청을 찾을 수 없습니다"));
        
        // 2. 수정 가능한 상태인지 확인
        // if (existingRequest.getStatus() != LeaveRequest.RequestStatus.REQUESTED) {
        //     throw new RuntimeException("수정할 수 없는 상태입니다");
        // }
        
        // 3. 기존 휴가 신청 삭제
        // leaveRequestRepository.delete(existingRequest);
        
        // 4. 새로운 휴가 신청 생성
        return createLeaveRequest(createDto);
    }
    
    /**
     * 휴가 신청을 조회합니다
     * @param requestId 휴가 신청 ID
     * @return 휴가 신청 응답 DTO
     */
    @Transactional(readOnly = true)
    public LeaveRequestResponseDto getLeaveRequest(Long requestId) {
        // LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
        //         .orElseThrow(() -> new RuntimeException("휴가 신청을 찾을 수 없습니다"));
        
        // return convertToResponseDto(leaveRequest);
        return null; // TODO: 실제 구현 필요
    }
    
    /**
     * 직원의 휴가 신청 목록을 조회합니다
     * @param employeeId 직원 ID
     * @return 휴가 신청 목록
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getLeaveRequestsByEmployee(Long employeeId) {
        // List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployeeId(employeeId);
        // return leaveRequests.stream()
        //         .map(this::convertToResponseDto)
        //         .toList();
        return null; // TODO: 실제 구현 필요
    }
    
    /**
     * 휴가 신청을 삭제합니다
     * @param requestId 휴가 신청 ID
     */
    public void deleteLeaveRequest(Long requestId) {
        // LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
        //         .orElseThrow(() -> new RuntimeException("휴가 신청을 찾을 수 없습니다"));
        
        // if (leaveRequest.getStatus() != LeaveRequest.RequestStatus.REQUESTED) {
        //     throw new RuntimeException("삭제할 수 없는 상태입니다");
        // }
        
        // leaveRequestRepository.delete(leaveRequest);
    }
    
    /**
     * 휴가 신청의 총 시간을 계산합니다
     * @param createDto 휴가 신청 생성 DTO
     * @return 총 휴가 시간 (시간 단위)
     */
    public double calculateTotalHours(CreateLeaveRequestDto createDto) {
        LocalDate startDate = createDto.getStartDate();
        LocalDate endDate = createDto.getEndDate();
        LocalTime startTime = createDto.getStartTime();
        LocalTime endTime = createDto.getEndTime();
        
        return calculateTotalHours(startDate, endDate, startTime, endTime);
    }
    
    /**
     * 휴가 신청의 총 시간을 계산합니다
     * @param startDate 시작일
     * @param endDate 종료일
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 총 휴가 시간 (시간 단위)
     */
    private double calculateTotalHours(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        // 시간이 지정되지 않은 경우 (전일 휴가)
        if (startTime == null || endTime == null) {
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            return daysBetween * WORK_HOURS_PER_DAY;
        }
        
        // 같은 날짜인 경우 (반차, 시간 휴가)
        if (startDate.equals(endDate)) {
            Duration duration = Duration.between(startTime, endTime);
            return duration.toHours() + (duration.toMinutes() % 60) / 60.0;
        }
        
        // 여러 날짜에 걸친 경우
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double totalHours = 0.0;
        
        // 첫째 날
        if (startTime != null) {
            Duration firstDayDuration = Duration.between(startTime, WORK_END_TIME);
            totalHours += firstDayDuration.toHours() + (firstDayDuration.toMinutes() % 60) / 60.0;
        } else {
            totalHours += WORK_HOURS_PER_DAY;
        }
        
        // 중간 날짜들 (전일)
        if (daysBetween > 2) {
            totalHours += (daysBetween - 2) * WORK_HOURS_PER_DAY;
        }
        
        // 마지막 날
        if (endTime != null) {
            Duration lastDayDuration = Duration.between(WORK_START_TIME, endTime);
            totalHours += lastDayDuration.toHours() + (lastDayDuration.toMinutes() % 60) / 60.0;
        } else {
            totalHours += WORK_HOURS_PER_DAY;
        }
        
        return totalHours;
    }
    
    /**
     * 휴가 신청을 검증합니다
     * @param createDto 휴가 신청 생성 DTO
     */
    private void validateLeaveRequest(CreateLeaveRequestDto createDto) {
        // 기본 검증
        if (createDto.getStartDate() == null || createDto.getEndDate() == null) {
            throw new RuntimeException("시작일과 종료일은 필수입니다");
        }
        
        if (createDto.getStartDate().isAfter(createDto.getEndDate())) {
            throw new RuntimeException("시작일은 종료일보다 이전이어야 합니다");
        }
        
        // 시간 검증
        LocalTime startTime = createDto.getStartTime();
        LocalTime endTime = createDto.getEndTime();
        
        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new RuntimeException("시작 시간은 종료 시간보다 이전이어야 합니다");
            }
            
            // 근무 시간 내인지 확인
            if (startTime.isBefore(WORK_START_TIME) || endTime.isAfter(WORK_END_TIME)) {
                throw new RuntimeException("휴가 시간은 근무 시간 내에 있어야 합니다");
            }
        }
    }
    
    /**
     * User 엔티티를 조회합니다 (TODO: User Service 연동 필요)
     * @param employeeId 직원 ID
     * @return User 엔티티
     */
    private User getUserById(Long employeeId) {
        // TODO: User Service에서 조회하거나 User Repository 사용
        // return userRepository.findById(employeeId)
        //         .orElseThrow(() -> new RuntimeException("직원을 찾을 수 없습니다"));
        return null; // 임시 반환
    }
    
    /**
     * LeaveRequest 엔티티를 응답 DTO로 변환합니다
     * @param leaveRequest 휴가 신청 엔티티
     * @return 응답 DTO
     */
    private LeaveRequestResponseDto convertToResponseDto(LeaveRequest leaveRequest) {
        double totalHours = calculateTotalHours(
            leaveRequest.getStartDate(), 
            leaveRequest.getEndDate(), 
            leaveRequest.getStartTime(), 
            leaveRequest.getEndTime()
        );
        
        return LeaveRequestResponseDto.builder()
                .requestId(leaveRequest.getRequestId())
                .employeeId(leaveRequest.getEmployee() != null ? leaveRequest.getEmployee().getId() : null)
                .employeeName(leaveRequest.getEmployee() != null ? leaveRequest.getEmployee().getName() : null)
                .leaveType(leaveRequest.getLeaveType())
                .leaveTypeName(leaveRequest.getLeaveType() != null ? leaveRequest.getLeaveType().getName() : null)
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .startTime(leaveRequest.getStartTime())
                .endTime(leaveRequest.getEndTime())
                .totalDays(leaveRequest.getTotalDays())
                .totalHours(totalHours)
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus() != null ? leaveRequest.getStatus().name() : null)
                .statusName(getStatusName(leaveRequest.getStatus()))
                .approverId(leaveRequest.getApprover() != null ? leaveRequest.getApprover().getId() : null)
                .approverName(leaveRequest.getApprover() != null ? leaveRequest.getApprover().getName() : null)
                .requestedAt(leaveRequest.getRequestedAt())
                .approvedAt(leaveRequest.getApprovedAt())
                .build();
    }
    
    /**
     * 상태명을 반환합니다
     * @param status 휴가 신청 상태
     * @return 상태명
     */
    private String getStatusName(LeaveRequest.RequestStatus status) {
        if (status == null) return null;
        
        return switch (status) {
            case REQUESTED -> "신청";
            case APPROVED -> "승인";
            case REJECTED -> "반려";
            case CANCELLED -> "취소";
        };
    }
}
