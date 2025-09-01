package com.hermes.attendanceservice.service.workmonitor;

import com.hermes.attendanceservice.client.UserServiceClient;
import com.hermes.attendanceservice.dto.workmonitor.WorkMonitorDto;
import com.hermes.attendanceservice.entity.attendance.Attendance;
import com.hermes.attendanceservice.entity.attendance.WorkStatus;
import com.hermes.attendanceservice.entity.leave.LeaveRequest;
import com.hermes.attendanceservice.entity.workmonitor.WorkMonitor;
import com.hermes.attendanceservice.repository.attendance.AttendanceRepository;
import com.hermes.attendanceservice.repository.leave.LeaveRepository;
import com.hermes.attendanceservice.repository.workmonitor.WorkMonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkMonitorService {
    
    private final WorkMonitorRepository workMonitorRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final UserServiceClient userServiceClient;
    
    /**
     * 특정 날짜의 근무 모니터링 데이터 조회
     */
    @Transactional(readOnly = true)
    public WorkMonitorDto getWorkMonitorByDate(LocalDate date) {
        Optional<WorkMonitor> workMonitor = workMonitorRepository.findByDate(date);
        
        if (workMonitor.isPresent()) {
            return convertToDto(workMonitor.get());
        } else {
            // 데이터가 없으면 실시간으로 계산하여 생성
            return generateWorkMonitorData(date);
        }
    }
    
    /**
     * 오늘 날짜의 근무 모니터링 데이터 조회
     */
    @Transactional(readOnly = true)
    public WorkMonitorDto getTodayWorkMonitor() {
        return getWorkMonitorByDate(LocalDate.now());
    }
    
    /**
     * 출석 버튼 클릭 시 근무 모니터링 데이터 갱신
     */
    public WorkMonitorDto updateWorkMonitorData(LocalDate date) {
        WorkMonitorDto workMonitorDto = generateWorkMonitorData(date);
        
        // 기존 데이터가 있으면 업데이트, 없으면 새로 생성
        Optional<WorkMonitor> existingMonitor = workMonitorRepository.findByDate(date);
        
        if (existingMonitor.isPresent()) {
            WorkMonitor workMonitor = existingMonitor.get();
            updateWorkMonitorFromDto(workMonitor, workMonitorDto);
            workMonitorRepository.save(workMonitor);
        } else {
            WorkMonitor newWorkMonitor = convertToEntity(workMonitorDto);
            workMonitorRepository.save(newWorkMonitor);
        }
        
        return workMonitorDto;
    }
    
    /**
     * 실시간으로 근무 모니터링 데이터 생성
     */
    private WorkMonitorDto generateWorkMonitorData(LocalDate date) {
        // 1. 전체 직원 수 조회 (UserService에서 가져옴)
        int totalEmployees = getTotalEmployees();
        
        // 2. 출석 데이터 조회
        List<Attendance> attendances = attendanceRepository.findByDate(date);
        
        int attendanceCount = 0; // 정상 출근, 재택, 출장, 외근
        int lateCount = 0; // 지각
        
        for (Attendance attendance : attendances) {
            // 출근 상태에 따른 분류
            switch (attendance.getAttendanceStatus()) {
                case REGULAR:
                    attendanceCount++;
                    break;
                case LATE:
                    lateCount++;
                    break;
                default:
                    break;
            }
            
            // 근무 상태에 따른 추가 분류
            switch (attendance.getWorkStatus()) {
                case REMOTE:
                case BUSINESS_TRIP:
                case OUT_OF_OFFICE:
                    attendanceCount++;
                    break;
                default:
                    break;
            }
        }
        
        // 3. 휴가 데이터 조회
        int vacationCount = getVacationCount(date);
        
        return WorkMonitorDto.builder()
                .date(date)
                .totalEmployees(totalEmployees)
                .attendanceCount(attendanceCount)
                .lateCount(lateCount)
                .vacationCount(vacationCount)
                .build();
    }
    
    /**
     * UserService에서 전체 직원 수 조회
     */
    private int getTotalEmployees() {
        try {
            // 현재 요청의 Authorization 헤더를 가져옴
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                
                if (authorization == null || authorization.trim().isEmpty()) {
                    log.warn("Authorization header is missing or empty");
                    return 0;
                }
                
                // UserService에서 전체 직원 수를 조회하는 API 호출
                Map<String, Object> response = userServiceClient.getTotalEmployees();
                return (Integer) response.get("totalUsers");
            } else {
                log.warn("Request context not available");
                return 0;
            }
        } catch (Exception e) {
            log.error("Failed to get total employees from UserService", e);
            return 0;
        }
    }
    
    /**
     * 특정 날짜의 휴가 신청 수 조회
     */
    private int getVacationCount(LocalDate date) {
        // 승인된 휴가 신청 중 해당 날짜에 포함되는 것들의 수를 조회
        List<LeaveRequest> approvedLeaves = leaveRepository.findByStatusAndDateRange(
            LeaveRequest.RequestStatus.APPROVED, date, date);
        return approvedLeaves.size();
    }
    

    
    /**
     * Entity를 DTO로 변환
     */
    private WorkMonitorDto convertToDto(WorkMonitor workMonitor) {
        return WorkMonitorDto.builder()
                .id(workMonitor.getId())
                .date(workMonitor.getDate())
                .totalEmployees(workMonitor.getTotalEmployees())
                .attendanceCount(workMonitor.getAttendanceCount())
                .lateCount(workMonitor.getLateCount())
                .vacationCount(workMonitor.getVacationCount())
                .build();
    }
    
    /**
     * DTO를 Entity로 변환
     */
    private WorkMonitor convertToEntity(WorkMonitorDto dto) {
        return WorkMonitor.builder()
                .date(dto.getDate())
                .totalEmployees(dto.getTotalEmployees())
                .attendanceCount(dto.getAttendanceCount())
                .lateCount(dto.getLateCount())
                .vacationCount(dto.getVacationCount())
                .build();
    }
    
    /**
     * Entity 업데이트
     */
    private void updateWorkMonitorFromDto(WorkMonitor workMonitor, WorkMonitorDto dto) {
        workMonitor.setTotalEmployees(dto.getTotalEmployees());
        workMonitor.setAttendanceCount(dto.getAttendanceCount());
        workMonitor.setLateCount(dto.getLateCount());
        workMonitor.setVacationCount(dto.getVacationCount());
    }
} 