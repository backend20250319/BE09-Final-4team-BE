package com.hermes.attendanceservice.service.leave;

import com.hermes.attendanceservice.dto.leave.EmployeeLeaveBalanceResponseDto;
import com.hermes.attendanceservice.dto.leave.EmployeeLeaveBalanceSummaryDto;
import com.hermes.attendanceservice.entity.leave.LeaveType;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeLeaveBalanceService {
    
    /**
     * 직원에게 연차 자동 부여 (입사일/승진일 기준)
     */
    List<EmployeeLeaveBalanceResponseDto> grantAnnualLeave(Long employeeId, LocalDate baseDate);
    
    /**
     * 연차 사용 (차감)
     */
    void useLeave(Long employeeId, LeaveType leaveType, Integer days);
    
    /**
     * 연차 복구 (취소 시)
     */
    void restoreLeave(Long employeeId, LeaveType leaveType, Integer days);
    
    /**
     * 직원의 특정 타입 잔여 연차 조회
     */
    Integer getRemainingLeave(Long employeeId, LeaveType leaveType);
    
    /**
     * 직원의 모든 타입 총 잔여 연차 조회
     */
    Integer getTotalRemainingLeave(Long employeeId);
    
    /**
     * 직원의 연차 잔액 상세 조회
     */
    List<EmployeeLeaveBalanceResponseDto> getLeaveBalances(Long employeeId);
    
    /**
     * 직원의 연차 잔액 요약 조회
     */
    EmployeeLeaveBalanceSummaryDto getLeaveBalanceSummary(Long employeeId);
    
    /**
     * 특정 직원의 연차 초기화 및 재부여
     */
    List<EmployeeLeaveBalanceResponseDto> resetAndGrantAnnualLeave(Long employeeId, LocalDate newGrantDate);
    
    /**
     * 모든 직원의 연차 초기화 및 재부여 
     */
    void resetAllEmployeesAnnualLeave(LocalDate newGrantDate);
} 