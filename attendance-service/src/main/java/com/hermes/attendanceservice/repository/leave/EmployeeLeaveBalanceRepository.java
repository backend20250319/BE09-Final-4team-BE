package com.hermes.attendanceservice.repository.leave;

import com.hermes.attendanceservice.entity.leave.EmployeeLeaveBalance;
import com.hermes.attendanceservice.entity.leave.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLeaveBalanceRepository extends JpaRepository<EmployeeLeaveBalance, Long> {
    
    /**
     * 직원 ID로 모든 연차 잔액 조회
     */
    List<EmployeeLeaveBalance> findByEmployeeId(Long employeeId);
    
    /**
     * 직원 ID와 연차 타입으로 연차 잔액 조회
     */
    Optional<EmployeeLeaveBalance> findByEmployeeIdAndLeaveType(Long employeeId, LeaveType leaveType);
    

    
    /**
     * 직원 ID와 연차 타입으로 총 잔여 연차 계산
     */
    @Query("SELECT COALESCE(SUM(elb.remainingDays), 0) FROM EmployeeLeaveBalance elb WHERE elb.employeeId = :employeeId AND elb.leaveType = :leaveType")
    Integer calculateTotalRemainingDays(@Param("employeeId") Long employeeId, 
                                       @Param("leaveType") LeaveType leaveType);
    
    /**
     * 직원 ID로 모든 타입의 총 잔여 연차 계산
     */
    @Query("SELECT COALESCE(SUM(elb.remainingDays), 0) FROM EmployeeLeaveBalance elb WHERE elb.employeeId = :employeeId")
    Integer calculateTotalRemainingDaysByEmployeeId(@Param("employeeId") Long employeeId);
    

    
    /**
     * 특정 직원의 특정 연차 타입 삭제 (년차 초기화 시 사용)
     */
    void deleteByEmployeeIdAndLeaveType(Long employeeId, LeaveType leaveType);
    
    /**
     * 특정 직원의 모든 연차 잔액 삭제 (년차 초기화 시 사용)
     */
    void deleteByEmployeeId(Long employeeId);
} 