package com.hermes.attendanceservice.repository;

import com.hermes.attendanceservice.entity.workpolicy.AnnualLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnualLeaveRepository extends JpaRepository<AnnualLeave, Long> {
    
    /**
     * 근무 정책 ID로 휴가 목록 조회
     */
    List<AnnualLeave> findByWorkPolicyId(Long workPolicyId);
    
    /**
     * 근무 정책 ID로 휴가 총일수 계산
     */
    @Query("SELECT COALESCE(SUM(a.leaveDays), 0) FROM AnnualLeave a WHERE a.workPolicy.id = :workPolicyId")
    Integer calculateTotalLeaveDaysByWorkPolicyId(@Param("workPolicyId") Long workPolicyId);
    
    /**
     * 근무 정책 ID로 공휴일 총일수 계산
     */
    @Query("SELECT COALESCE(SUM(a.holidayDays), 0) FROM AnnualLeave a WHERE a.workPolicy.id = :workPolicyId")
    Integer calculateTotalHolidayDaysByWorkPolicyId(@Param("workPolicyId") Long workPolicyId);
} 
