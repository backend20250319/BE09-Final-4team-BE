package com.hermes.hrmanagementservice.repository;

import com.hermes.hrmanagementservice.entity.attendance.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);
    List<Attendance> findAllByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
} 