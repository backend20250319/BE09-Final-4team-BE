package com.hermes.attendanceservice.repository.attendance;

import com.hermes.attendanceservice.entity.attendance.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);
    List<Attendance> findAllByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
    List<Attendance> findByDate(LocalDate date);
    
    // 출근했지만 퇴근하지 않은 모든 기록 조회 (자동 퇴근 처리용)
    List<Attendance> findAllByCheckInIsNotNullAndCheckOutIsNullAndDate(LocalDate date);
} 