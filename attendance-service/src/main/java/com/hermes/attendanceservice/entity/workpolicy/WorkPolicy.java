package com.hermes.attendanceservice.entity.workpolicy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name; // 근무정책 이름 (처음 설정할 때 근무 정책 이름 기입)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkType type; // 고정, 교대, 시차, 선택
    
    @Enumerated(EnumType.STRING)
    @Column(name = "work_cycle")
    private WorkCycle workCycle; // 1주, 2주, 3주, 4주, 1개월 (선택 근무에만 적용, nullable)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "start_day_of_week", nullable = false)
    private StartDayOfWeek startDayOfWeek; // 근무 시작 요일 (월요일)
    
    @Column(name = "work_cycle_start_day")
    private Integer workCycleStartDay; // 근무 주기 시작일(1~31일, 선택 근무 용도, nullable)
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "work_policy_work_days", 
                    joinColumns = @JoinColumn(name = "work_policy_id"))
    @Column(name = "work_day")
    private List<StartDayOfWeek> workDays; // 필수 근무 요일 리스트(월요일)
    
    @Column(name = "weekly_working_days")
    private Integer weeklyWorkingDays; // 주간근무일수(교대 근무 용도, nullable)
    
    @Column(name = "start_time")
    private LocalTime startTime; // 출근 시간 (nullable)
    
    @Column(name = "start_time_end")
    private LocalTime startTimeEnd; // 출근 시간 범위 끝(시차 근무시 필수, 다른 근무 타입시 nullable)
    
    @Column(name = "work_hours", nullable = false)
    private Integer workHours; // 1일근무 시간 (시간 단위)
    
    @Column(name = "work_minutes", nullable = false)
    private Integer workMinutes; // 1일근무 시간 (분단위)
    
    @Column(name = "core_time_start")
    private LocalTime coreTimeStart; // 코어 타임시작 (선택 근무 용도, nullable)
    
    @Column(name = "core_time_end")
    private LocalTime coreTimeEnd; // 코어 타임끝(선택 근무 용도, nullable)
    
    @Column(name = "break_start_time", nullable = false)
    private LocalTime breakStartTime; // 휴게 시작 시간
    
    @Column(name = "break_end_time")
    private LocalTime breakEndTime; // 휴게 종료 시간
    
    @Column(name = "break_minutes")
    private Integer breakMinutes; // 휴게 시간 (분)
    
    @Column(name = "avg_work_time")
    private LocalTime avgWorkTime; // 평균 근무시간 (선택 근무 용도, nullable)
    
    @Column(name = "total_required_minutes", nullable = false)
    private Integer totalRequiredMinutes; // 단위기간 기준 근로 시간 (주동기준)
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "work_policy_holiday_days", 
                    joinColumns = @JoinColumn(name = "work_policy_id"))
    @Column(name = "holiday_day")
    private List<StartDayOfWeek> holidayDays; // 휴일 요일 리스트
    
    @ElementCollection
    @CollectionTable(name = "work_policy_holidays", 
                    joinColumns = @JoinColumn(name = "work_policy_id"))
    @Column(name = "holiday_date")
    private List<String> holidays; // 휴일 날짜 리스트
    
    @Column(name = "is_holiday_fixed")
    @Builder.Default
    private Boolean isHolidayFixed = true; // 휴일 고정 여부
    
    @Column(name = "is_break_fixed")
    @Builder.Default
    private Boolean isBreakFixed = true; // 휴식 시간 고정 여부
    
    @OneToMany(mappedBy = "workPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnnualLeave> annualLeaves = new ArrayList<>();
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    /**
     * 선택 근무인지 확인
     */
    public boolean isOptionalWork() {
        return type == WorkType.OPTIONAL;
    }
    
    /**
     * 교대 근무인지 확인
     */
    public boolean isShiftWork() {
        return type == WorkType.SHIFT;
    }
    
    /**
     * 시차 근무인지 확인
     */
    public boolean isFlexibleWork() {
        return type == WorkType.FLEXIBLE;
    }
    
    /**
     * 고정 근무인지 확인
     */
    public boolean isFixedWork() {
        return type == WorkType.FIXED;
    }
    
    /**
     * 일근무 시간을 분단위로 반환
     */
    public int getTotalWorkMinutes() {
        return (workHours * 60) + workMinutes;
    }
    
    /**
     * 노동법준수여부 확인 (1개월 160시간, 주단위기준)
     */
    public boolean isCompliantWithLaborLaw() {
        if (workCycle == WorkCycle.ONE_MONTH) {
            // 1개월 기준: 160시간 = 9600분
            return totalRequiredMinutes <= 9600;
        } else {
            // 주기준: 40시간 = 2400분
            return totalRequiredMinutes <= 2400;
        }
    }
    
    /**
     * 시차 근무 설정 유효성 검증
     * 시차 근무인 경우 startTime과 startTimeEnd가 모두 필수
     */
    public boolean isValidFlexibleWorkSettings() {
        if (isFlexibleWork()) {
            return startTime != null && startTimeEnd != null;
        }
        return true; // 시차 근무가 아닌 경우 항상 유효
    }
    
    /**
     * 시차 근무 출근 가능 시간대 설정 여부 확인
     */
    public boolean hasFlexibleWorkTimeRange() {
        return isFlexibleWork() && startTime != null && startTimeEnd != null;
    }
    
    /**
     * 시차 근무 출근 가능 시간대 유효성 검증
     * startTime이 startTimeEnd보다 이전이어야 함
     */
    public boolean isValidFlexibleWorkTimeRange() {
        if (!hasFlexibleWorkTimeRange()) {
            return false;
        }
        return startTime.isBefore(startTimeEnd);
    }
} 
