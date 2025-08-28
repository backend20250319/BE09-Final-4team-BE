package com.hermes.attendanceservice.repository.workschedule;

import com.hermes.attendanceservice.entity.workschedule.WorkTimeAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkTimeAdjustmentRepository extends JpaRepository<WorkTimeAdjustment, Long> {
    
    // 사용자별 근무 시간 조정 조회
    List<WorkTimeAdjustment> findByUserIdOrderByAdjustDateDesc(Long userId);
    
    // 사용자별 특정 기간 근무 시간 조정 조회
    List<WorkTimeAdjustment> findByUserIdAndAdjustDateBetweenOrderByAdjustDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);
    
    // 특정 날짜의 근무 시간 조정 조회
    List<WorkTimeAdjustment> findByAdjustDate(LocalDate adjustDate);
    
    // 사용자별 특정 날짜의 근무 시간 조정 조회
    Optional<WorkTimeAdjustment> findByUserIdAndAdjustDate(Long userId, LocalDate adjustDate);
    
    // 페이지네이션을 위한 사용자별 근무 시간 조정 조회
    Page<WorkTimeAdjustment> findByUserId(Long userId, Pageable pageable);
    
    // 사용자별 조정 유형별 조회
    List<WorkTimeAdjustment> findByUserIdAndAdjustTypeOrderByAdjustDateDesc(Long userId, String adjustType);
    
    // 월별 근무 시간 조정 통계
    @Query("SELECT COUNT(w) FROM WorkTimeAdjustment w WHERE w.userId = :userId " +
           "AND YEAR(w.adjustDate) = :year AND MONTH(w.adjustDate) = :month")
    long countByUserIdAndYearMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
} 