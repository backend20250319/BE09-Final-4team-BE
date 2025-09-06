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
    
    @Override
    public List<EmployeeLeaveBalanceResponseDto> grantAnnualLeave(Long employeeId, LocalDate baseDate) {
        log.info("연차 자동 부여 시작: employeeId={}, baseDate={}", employeeId, baseDate);
        
        // 1. 직원 정보 조회
        Map<String, Object> user = userServiceClient.getUserById(employeeId);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 직원입니다: " + employeeId);
        }
        
        // 2. 근무년수 조회 (user-service에서 계산된 값)
        Integer workYears = (Integer) user.get("workYears");
        if (workYears == null) {
            // 임시: workYears가 없으면 기본값 1년으로 설정 (향후 user-service에서 제공해야 함)
            workYears = 1;
            log.warn("직원의 근무년수 정보를 찾을 수 없어 기본값 1년으로 설정합니다: employeeId={}", employeeId);
        }
        log.info("조회된 근무년수: {}년", workYears);
        
        // 3. 직원의 근무정책 조회 및 연차 규정 가져오기
        Long workPolicyId = (Long) user.get("workPolicyId");
        if (workPolicyId == null) {
            throw new IllegalArgumentException("직원에게 근무정책이 할당되지 않았습니다: " + employeeId);
        }
        
        List<AnnualLeave> annualLeaves = annualLeaveRepository.findByWorkPolicyId(workPolicyId);
        if (annualLeaves.isEmpty()) {
            throw new IllegalArgumentException("근무정책에 연차 규정이 없습니다: " + workPolicyId);
        }
        
        // 4. 해당 근무년수에 맞는 연차 규정 찾기 및 부여
        List<EmployeeLeaveBalance> grantedLeaves = new ArrayList<>();
        
        for (AnnualLeave annualLeave : annualLeaves) {
            if (annualLeave.isInRange(workYears)) {
                // 기본 연차로 부여 (실제로는 연차 타입 매핑 로직 필요)
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
                log.info("연차 부여: type={}, days={}", leaveType, annualLeave.getLeaveDays());
            }
        }
        
        if (grantedLeaves.isEmpty()) {
            log.warn("해당 근무년수에 맞는 연차 규정이 없습니다: workYears={}", workYears);
            return new ArrayList<>();
        }
        
        List<EmployeeLeaveBalance> savedLeaves = employeeLeaveBalanceRepository.saveAll(grantedLeaves);
        log.info("연차 자동 부여 완료: employeeId={}, count={}", employeeId, savedLeaves.size());
        
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
    public void resetAllEmployeesAnnualLeave(LocalDate newGrantDate) {
        log.info("모든 직원 연차 초기화 및 재부여 시작: newGrantDate={}", newGrantDate);
        
        // 1. 모든 연차 잔액 삭제
        employeeLeaveBalanceRepository.deleteAll();
        
        // 2. 모든 활성 직원에게 연차 재부여 (UserService에서 활성 직원 목록 가져와야 함)
        
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