package com.hermes.workpolicyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private String name; // 근무유형 이름 (맨 처음 설정할때 근무 정책 이름 기입)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkType type; // 고정, 교대, 시차, 선택
    
    @Enumerated(EnumType.STRING)
    @Column(name = "work_cycle")
    private WorkCycle workCycle; // 1주, 2주, 3주, 4주, 1개월 등 (선택 근무만 사용, nullable)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "start_day_of_week", nullable = false)
    private StartDayOfWeek startDayOfWeek; // 근무 시작 요일 (월~일)
    
    @Column(name = "work_cycle_start_day")
    private Integer workCycleStartDay; // 근무 주기 시작일 (1~31일, 선택 근무 전용, nullable)
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "work_policy_work_days", 
                    joinColumns = @JoinColumn(name = "work_policy_id"))
    @Column(name = "work_day")
    private List<StartDayOfWeek> workDays; // 필수 근무 요일 리스트 (월~일)
    
    @Column(name = "weekly_working_days")
    private Integer weeklyWorkingDays; // 한 주 근무일 수 (교대 근무 전용, nullable)
    
    @Column(name = "start_time")
    private LocalTime startTime; // 출근 시간 (nullable)
    
    @Column(name = "start_time_end")
    private LocalTime startTimeEnd; // 출근 시간 범위 끝 (시차 근무용, nullable)
    
    @Column(name = "work_hours", nullable = false)
    private Integer workHours; // 1일 근무 시간 (시간 단위)
    
    @Column(name = "work_minutes", nullable = false)
    private Integer workMinutes; // 1일 근무 시간 (분 단위)
    
    @Column(name = "core_time_start")
    private LocalTime coreTimeStart; // 코어 타임 시작 (선택 근무 전용, nullable)
    
    @Column(name = "core_time_end")
    private LocalTime coreTimeEnd; // 코어 타임 끝 (선택 근무 전용, nullable)
    
    @Column(name = "break_start_time", nullable = false)
    private LocalTime breakStartTime; // 휴게 시작 시간
    
    @Column(name = "avg_work_time")
    private LocalTime avgWorkTime; // 평균 근무시간 (선택 근무 전용, nullable)
    
    @Column(name = "total_required_minutes", nullable = false)
    private Integer totalRequiredMinutes; // 단위기간 기준 근로 시간 (노동법 기준)
    
    @OneToMany(mappedBy = "workPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnnualLeave> annualLeaves = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
     * 총 근무 시간을 분 단위로 반환
     */
    public int getTotalWorkMinutes() {
        return (workHours * 60) + workMinutes;
    }
    
    /**
     * 노동법 준수 여부 확인 (1개월 160시간, 그 외 주 기준)
     */
    public boolean isCompliantWithLaborLaw() {
        if (workCycle == WorkCycle.ONE_MONTH) {
            // 1개월 기준: 160시간 = 9600분
            return totalRequiredMinutes <= 9600;
        } else {
            // 주 기준: 40시간 = 2400분
            return totalRequiredMinutes <= 2400;
        }
    }
}
