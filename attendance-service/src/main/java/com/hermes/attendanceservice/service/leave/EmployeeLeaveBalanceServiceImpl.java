package com.hermes.attendanceservice.service.leave;

import com.hermes.attendanceservice.dto.leave.EmployeeLeaveBalanceResponseDto;
import com.hermes.attendanceservice.dto.leave.EmployeeLeaveBalanceSummaryDto;
import com.hermes.attendanceservice.entity.leave.EmployeeLeaveBalance;
import com.hermes.attendanceservice.entity.leave.LeaveType;
import com.hermes.attendanceservice.entity.workpolicy.AnnualLeave;
import com.hermes.attendanceservice.repository.leave.EmployeeLeaveBalanceRepository;
import com.hermes.attendanceservice.repository.workpolicy.AnnualLeaveRepository;
import com.hermes.attendanceservice.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeLeaveBalanceServiceImpl implements EmployeeLeaveBalanceService {
    
    private final EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;
    private final AnnualLeaveRepository annualLeaveRepository;
    private final UserServiceClient userServiceClient;
    
    /**
     * 매일 자정에 실행되는 자동 연차 부여 스케줄러
     * 근무년수가 변경된 직원들에게 자동으로 연차를 재부여
     */
    @Scheduled(cron = "0 10 0 * * ?", zone = "Asia/Seoul") // 자정 10분 후 실행
    public void scheduleAnnualLeaveGrant() {
        log.info("자동 연차 부여 스케줄러 실행 시작");
        
        try {
            grantAnnualLeaveToAllEmployees();
            log.info("자동 연차 부여 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("자동 연차 부여 스케줄러 실행 실패", e);
        }
    }
    
    @Override
    public List<EmployeeLeaveBalanceResponseDto> grantAnnualLeave(Long employeeId, LocalDate baseDate) {
        log.info("연차 자동 부여 시작: employeeId={}, baseDate={}", employeeId, baseDate);
        
        // 1. 직원 정보 조회
        Map<String, Object> user = userServiceClient.getUserById(employeeId);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 직원입니다: " + employeeId);
        }
        
        // 2. 근무년수 조회 (user-service의 전용 API 사용)
        Integer workYears = null;
        try {
            Map<String, Integer> workYearsResponse = userServiceClient.getUserWorkYears(employeeId);
            workYears = workYearsResponse.get("workYears");
            log.info("user-service에서 조회된 근무년수: {}년", workYears);
        } catch (Exception e) {
            log.warn("user-service에서 근무년수 조회 실패, 사용자 정보에서 확인: {}", e.getMessage());
            // fallback: 기존 사용자 정보에서 workYears 확인
            Object workYearsObj = user.get("workYears");
            if (workYearsObj != null) {
                workYears = Integer.valueOf(workYearsObj.toString());
            }
        }
        
        if (workYears == null) {
            // 최후 수단: 기본값 0년으로 설정
            workYears = 0;
            log.warn("직원의 근무년수 정보를 찾을 수 없어 기본값 0년으로 설정합니다: employeeId={}", employeeId);
        }
        
        log.info("최종 확인된 근무년수: {}년", workYears);
        
        // 3. 직원의 근무정책 조회 및 연차 규정 가져오기
        Long workPolicyId = null;
        Object workPolicyIdObj = user.get("workPolicyId");
        if (workPolicyIdObj != null) {
            workPolicyId = Long.valueOf(workPolicyIdObj.toString());
        }
        
        if (workPolicyId == null) {
            throw new IllegalArgumentException("직원에게 근무정책이 할당되지 않았습니다: " + employeeId);
        }
        
        List<AnnualLeave> annualLeaves = annualLeaveRepository.findByWorkPolicyId(workPolicyId);
        if (annualLeaves.isEmpty()) {
            throw new IllegalArgumentException("근무정책에 연차 규정이 없습니다: " + workPolicyId);
        }
        
        // 4. 기존 연차 잔액 삭제 (새로운 연차 부여 전)
        employeeLeaveBalanceRepository.deleteByEmployeeId(employeeId);
        log.info("기존 연차 잔액 삭제 완료: employeeId={}", employeeId);
        
        // 5. 해당 근무년수에 맞는 연차 규정 찾기 및 부여
        List<EmployeeLeaveBalance> grantedLeaves = new ArrayList<>();
        
        for (AnnualLeave annualLeave : annualLeaves) {
            if (annualLeave.isInRange(workYears)) {
                // 연차 타입 매핑
                LeaveType leaveType = mapToLeaveType(annualLeave.getName());
                
                EmployeeLeaveBalance leaveBalance = EmployeeLeaveBalance.builder()
                        .employeeId(employeeId)
                        .leaveType(leaveType)
                        .totalLeaveDays(annualLeave.getLeaveDays())
                        .remainingDays(annualLeave.getLeaveDays())
                        .usedLeaveDays(0)
                        .workYears(workYears)
                        .build();
                
                grantedLeaves.add(leaveBalance);
                log.info("연차 부여: employeeId={}, type={}, days={}, workYears={}", 
                        employeeId, leaveType, annualLeave.getLeaveDays(), workYears);
            }
        }
        
        if (grantedLeaves.isEmpty()) {
            log.warn("해당 근무년수에 맞는 연차 규정이 없습니다: employeeId={}, workYears={}", employeeId, workYears);
            return new ArrayList<>();
        }
        
        List<EmployeeLeaveBalance> savedLeaves = employeeLeaveBalanceRepository.saveAll(grantedLeaves);
        log.info("연차 자동 부여 완료: employeeId={}, count={}, workYears={}년", employeeId, savedLeaves.size(), workYears);
        
        return savedLeaves.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void useLeave(Long employeeId, LeaveType leaveType, Integer days) {
        log.info("연차 사용 시작: employeeId={}, leaveType={}, days={}", employeeId, leaveType, days);
        
        Optional<EmployeeLeaveBalance> balanceOpt = employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveType(employeeId, leaveType);
        
        if (balanceOpt.isEmpty()) {
            throw new IllegalArgumentException("사용 가능한 연차가 없습니다: employeeId=" + employeeId + ", leaveType=" + leaveType);
        }
        
        EmployeeLeaveBalance balance = balanceOpt.get();
        balance.useLeave(days);
        
        employeeLeaveBalanceRepository.save(balance);
        log.info("연차 사용 완료: 잔여 연차={}", balance.getRemainingDays());
    }
    
    @Override
    public void restoreLeave(Long employeeId, LeaveType leaveType, Integer days) {
        log.info("연차 복구 시작: employeeId={}, leaveType={}, days={}", employeeId, leaveType, days);
        
        Optional<EmployeeLeaveBalance> balanceOpt = employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveType(employeeId, leaveType);
        
        if (balanceOpt.isEmpty()) {
            throw new IllegalArgumentException("연차 잔액이 없습니다: employeeId=" + employeeId + ", leaveType=" + leaveType);
        }
        
        EmployeeLeaveBalance balance = balanceOpt.get();
        balance.restoreLeave(days);
        
        employeeLeaveBalanceRepository.save(balance);
        log.info("연차 복구 완료: 잔여 연차={}", balance.getRemainingDays());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getRemainingLeave(Long employeeId, LeaveType leaveType) {
        return employeeLeaveBalanceRepository.calculateTotalRemainingDays(employeeId, leaveType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getTotalRemainingLeave(Long employeeId) {
        return employeeLeaveBalanceRepository.calculateTotalRemainingDaysByEmployeeId(employeeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLeaveBalanceResponseDto> getLeaveBalances(Long employeeId) {
        List<EmployeeLeaveBalance> balances = employeeLeaveBalanceRepository.findByEmployeeId(employeeId);
        return balances.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeLeaveBalanceSummaryDto getLeaveBalanceSummary(Long employeeId) {
        List<EmployeeLeaveBalance> balances = employeeLeaveBalanceRepository.findByEmployeeId(employeeId);
        
        int totalRemaining = balances.stream().mapToInt(EmployeeLeaveBalance::getRemainingDays).sum();
        int totalUsed = balances.stream().mapToInt(EmployeeLeaveBalance::getUsedLeaveDays).sum();
        int totalGranted = balances.stream().mapToInt(EmployeeLeaveBalance::getTotalLeaveDays).sum();
        
        // 타입별 잔여 연차
        int basicRemaining = balances.stream()
                .filter(b -> b.getLeaveType() == LeaveType.BASIC_ANNUAL)
                .mapToInt(EmployeeLeaveBalance::getRemainingDays).sum();
        
        int compensationRemaining = balances.stream()
                .filter(b -> b.getLeaveType() == LeaveType.COMPENSATION_ANNUAL)
                .mapToInt(EmployeeLeaveBalance::getRemainingDays).sum();
        
        int specialRemaining = balances.stream()
                .filter(b -> b.getLeaveType() == LeaveType.SPECIAL_ANNUAL)
                .mapToInt(EmployeeLeaveBalance::getRemainingDays).sum();
        
        double overallUsageRate = totalGranted > 0 ? (double) totalUsed / totalGranted : 0.0;
        
        return EmployeeLeaveBalanceSummaryDto.builder()
                .employeeId(employeeId)
                .totalRemainingDays(totalRemaining)
                .totalUsedDays(totalUsed)
                .totalGrantedDays(totalGranted)
                .leaveBalances(balances.stream().map(this::convertToResponseDto).collect(Collectors.toList()))
                .basicAnnualRemaining(basicRemaining)
                .compensationAnnualRemaining(compensationRemaining)
                .specialAnnualRemaining(specialRemaining)
                .overallUsageRate(overallUsageRate)
                .build();
    }
    
    @Override
    public List<EmployeeLeaveBalanceResponseDto> resetAndGrantAnnualLeave(Long employeeId, LocalDate newGrantDate) {
        log.info("직원 연차 초기화 및 재부여 시작: employeeId={}, newGrantDate={}", employeeId, newGrantDate);
        
        // 1. 기존 연차 잔액 삭제
        employeeLeaveBalanceRepository.deleteByEmployeeId(employeeId);
        log.info("기존 연차 잔액 삭제 완료: employeeId={}", employeeId);
        
        // 2. 새로운 연차 부여
        return grantAnnualLeave(employeeId, newGrantDate);
    }
    
    @Override
    public List<EmployeeLeaveBalanceResponseDto> grantAnnualLeaveByWorkYears(Long employeeId) {
        log.info("근무년수 기반 연차 자동 부여 시작: employeeId={}", employeeId);
        return grantAnnualLeave(employeeId, LocalDate.now());
    }
    
    @Override
    public void grantAnnualLeaveToAllEmployees() {
        log.info("모든 직원에게 근무년수 기반 연차 부여 시작");
        
        try {
            // 1. 전체 직원 수 조회
            Map<String, Object> totalEmployeesResponse = userServiceClient.getTotalEmployees();
            Long totalEmployees = (Long) totalEmployeesResponse.get("totalUsers");
            
            if (totalEmployees == null || totalEmployees == 0) {
                log.warn("부여할 직원이 없습니다.");
                return;
            }
            
            log.info("총 {}명의 직원에게 연차 부여 시작", totalEmployees);
            
            // 2. 페이지별로 직원들을 가져와서 연차 부여 (메모리 효율성을 위해)
            int pageSize = 100; // 한 번에 처리할 직원 수
            int totalPages = (int) Math.ceil((double) totalEmployees / pageSize);
            int successCount = 0;
            int failCount = 0;
            
            for (int page = 0; page < totalPages; page++) {
                try {
                    // 실제로는 UserServiceClient에 페이징 API가 필요하지만, 
                    // 현재는 간단히 ID 범위로 처리
                    for (long employeeId = page * pageSize + 1; employeeId <= Math.min((page + 1) * pageSize, totalEmployees); employeeId++) {
                        try {
                            grantAnnualLeaveByWorkYears(employeeId);
                            successCount++;
                            log.debug("직원 연차 부여 성공: employeeId={}", employeeId);
                        } catch (Exception e) {
                            failCount++;
                            log.warn("직원 연차 부여 실패: employeeId={}, error={}", employeeId, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("페이지 {} 처리 중 오류 발생: {}", page, e.getMessage());
                }
            }
            
            log.info("모든 직원 연차 부여 완료: 성공={}명, 실패={}명", successCount, failCount);
            
        } catch (Exception e) {
            log.error("모든 직원 연차 부여 중 오류 발생", e);
            throw new RuntimeException("모든 직원 연차 부여 실패", e);
        }
    }
    
    @Override
    public void resetAllEmployeesAnnualLeave(LocalDate newGrantDate) {
        log.info("모든 직원 연차 초기화 및 재부여 시작: newGrantDate={}", newGrantDate);
        
        // 1. 모든 연차 잔액 삭제
        employeeLeaveBalanceRepository.deleteAll();
        log.info("모든 연차 잔액 삭제 완료");
        
        // 2. 모든 활성 직원에게 연차 재부여
        grantAnnualLeaveToAllEmployees();
        
        log.info("모든 직원 연차 초기화 및 재부여 완료");
    }
    

    
    // Helper methods
    
    private EmployeeLeaveBalanceResponseDto convertToResponseDto(EmployeeLeaveBalance balance) {
        return EmployeeLeaveBalanceResponseDto.builder()
                .id(balance.getId())
                .employeeId(balance.getEmployeeId())
                .leaveType(balance.getLeaveType())
                .leaveTypeName(getLeaveTypeName(balance.getLeaveType()))
                .totalLeaveDays(balance.getTotalLeaveDays())
                .usedLeaveDays(balance.getUsedLeaveDays())
                .remainingDays(balance.getRemainingDays())
                .workYears(balance.getWorkYears())
                .usageRate(balance.getUsageRate())
                .createdAt(balance.getCreatedAt())
                .updatedAt(balance.getUpdatedAt())
                .build();
    }
    
    private String getLeaveTypeName(LeaveType leaveType) {
        switch (leaveType) {
            case BASIC_ANNUAL: return "기본 연차";
            case COMPENSATION_ANNUAL: return "보상 연차";
            case SPECIAL_ANNUAL: return "특별 연차";
            default: return leaveType.name();
        }
    }
    
    private LeaveType mapToLeaveType(String annualLeaveName) {
        // 연차 규정 이름을 LeaveType으로 매핑
        if (annualLeaveName.contains("기본") || annualLeaveName.contains("일반")) {
            return LeaveType.BASIC_ANNUAL;
        } else if (annualLeaveName.contains("보상")) {
            return LeaveType.COMPENSATION_ANNUAL;
        } else if (annualLeaveName.contains("특별")) {
            return LeaveType.SPECIAL_ANNUAL;
        }
        return LeaveType.BASIC_ANNUAL; // 기본값
    }
} 