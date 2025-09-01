package com.hermes.attendanceservice.repository.workschedule;

import com.hermes.attendanceservice.entity.workschedule.Schedule;
import com.hermes.attendanceservice.entity.workschedule.ScheduleType;
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
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // 사용자별 스케줄 조회
    List<Schedule> findByUserIdAndStatusOrderByStartDateAscStartTimeAsc(Long userId, String status);
    
    // 사용자별 특정 기간 스케줄 조회
    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.status = :status " +
           "AND ((s.startDate BETWEEN :startDate AND :endDate) OR (s.endDate BETWEEN :startDate AND :endDate) " +
           "OR (s.startDate <= :startDate AND s.endDate >= :endDate)) " +
           "ORDER BY s.startDate ASC, s.startTime ASC")
    List<Schedule> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    // 사용자별 오늘 스케줄 조회
    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.status = :status " +
           "AND s.startDate <= :today AND s.endDate >= :today " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findTodaySchedulesByUserId(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("today") LocalDate today
    );
    
    // 사용자별 스케줄 타입별 조회
    List<Schedule> findByUserIdAndScheduleTypeAndStatusOrderByStartDateAscStartTimeAsc(
            Long userId, ScheduleType scheduleType, String status);
    
    // 반복 스케줄 조회
    List<Schedule> findByUserIdAndIsRecurringTrueAndStatusOrderByStartDateAscStartTimeAsc(
            Long userId, String status);
    
    // 근무 정책과 연동된 스케줄 조회
    List<Schedule> findByUserIdAndWorkPolicyIdIsNotNullAndStatusOrderByStartDateAscStartTimeAsc(
            Long userId, String status);
    
    // 페이지네이션을 위한 사용자별 스케줄 조회
    Page<Schedule> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
    
    // 스케줄 중복 확인 (같은 시간대에 다른 스케줄이 있는지)
    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.userId = :userId AND s.status = 'ACTIVE' " +
           "AND s.id != :scheduleId " +
           "AND ((s.startDate <= :endDate AND s.endDate >= :startDate) " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime) OR s.isAllDay = true))")
    boolean existsConflictingSchedule(
            @Param("userId") Long userId,
            @Param("scheduleId") Long scheduleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
    
    // 스케줄 존재 여부 확인
    Optional<Schedule> findByIdAndUserId(Long id, Long userId);
    
    // 사용자별 활성 스케줄 개수
    long countByUserIdAndStatus(Long userId, String status);
    
    // 특정 날짜의 사용자 근무 스케줄 조회
    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.scheduleType = :scheduleType " +
           "AND s.status = 'ACTIVE' AND s.startDate <= :date AND s.endDate >= :date " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findByUserIdAndDateAndScheduleType(
            @Param("userId") Long userId, 
            @Param("date") LocalDate date, 
            @Param("scheduleType") ScheduleType scheduleType
    );
} 