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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@EnableScheduling
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
        
        int attendanceCount = 0; // 정상 출근 + 재택 + 출장 + 외근
        int lateCount = 0; // 지각
        
        for (Attendance attendance : attendances) {
            // 체크인이 있는 경우만 집계 (실제 출근한 사람)
            if (attendance.getCheckIn() != null) {
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
            }
        }
        
        // 3. 휴가 데이터 조회
        int vacationCount = getVacationCount(date);
        
        log.info("Generated work monitor data for {}: total={}, attendance={}, late={}, vacation={}", 
                date, totalEmployees, attendanceCount, lateCount, vacationCount);
        
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
    
    /**
     * 매일 자정에 전날 데이터 최종 집계 및 오늘 데이터 초기화
     * 새로운 날짜가 시작될 때마다 실행
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void dailyWorkMonitorUpdate() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate today = LocalDate.now();
            
            log.info("Starting daily work monitor update for yesterday: {}", yesterday);
            
            // 전날 데이터 최종 집계
            updateWorkMonitorData(yesterday);
            
            // 오늘 데이터 초기화 (새로운 날짜 시작)
            Optional<WorkMonitor> todayMonitor = workMonitorRepository.findByDate(today);
            if (!todayMonitor.isPresent()) {
                // 오늘 날짜의 초기 데이터 생성
                WorkMonitorDto initialData = generateWorkMonitorData(today);
                WorkMonitor newMonitor = convertToEntity(initialData);
                workMonitorRepository.save(newMonitor);
                log.info("Created initial work monitor data for today: {}", today);
            }
            
        } catch (Exception e) {
            log.error("Failed to perform daily work monitor update", e);
        }
    }
    
    /**
     * 매 30분마다 오늘 데이터 갱신 (실시간성 확보)
     */
    @Scheduled(fixedRate = 1800000) // 30분마다 (30 * 60 * 1000 ms)
    public void periodicWorkMonitorUpdate() {
        try {
            LocalDate today = LocalDate.now();
            log.debug("Performing periodic work monitor update for today: {}", today);
            updateWorkMonitorData(today);
        } catch (Exception e) {
            log.error("Failed to perform periodic work monitor update", e);
        }
    }
    
    /**
     * 출근/퇴근 이벤트 발생 시 즉시 호출할 수 있는 메서드
     * AttendanceService에서 체크인/체크아웃 후 호출하여 실시간 업데이트
     */
    public void refreshTodayWorkMonitor() {
        try {
            LocalDate today = LocalDate.now();
            log.info("Refreshing work monitor data due to attendance event: {}", today);
            updateWorkMonitorData(today);
        } catch (Exception e) {
            log.error("Failed to refresh today's work monitor data", e);
        }
    }
} 