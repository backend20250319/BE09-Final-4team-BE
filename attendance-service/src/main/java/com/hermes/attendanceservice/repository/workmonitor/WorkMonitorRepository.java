package com.hermes.attendanceservice.repository.workmonitor;

import com.hermes.attendanceservice.entity.workmonitor.WorkMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkMonitorRepository extends JpaRepository<WorkMonitor, Long> {
    
    // 특정 날짜의 근무 모니터링 데이터 조회
    Optional<WorkMonitor> findByDate(LocalDate date);
    
    // 특정 기간의 근무 모니터링 데이터 조회
    List<WorkMonitor> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    // 오늘 날짜의 데이터가 존재하는지 확인
    boolean existsByDate(LocalDate date);
} 