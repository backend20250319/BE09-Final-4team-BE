package com.hermes.attendanceservice.entity.workpolicy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "annual_leave")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualLeave {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_policy_id", nullable = false)
    private WorkPolicy workPolicy;
    
    @Column(nullable = false, length = 100)
    private String name; // 휴가 종류 이름 (예: "기본 휴가", "연기 근속 휴가", "특별 휴가")
    
    @Column(name = "min_years", nullable = false)
    private Integer minYears; // 최소 근무년수 (0, 1, 2, 3...)
    
    @Column(name = "max_years", nullable = false)
    private Integer maxYears; // 최대 근무년수 (1, 2, 3, 99...)
    
    @Column(name = "leave_days", nullable = false)
    private Integer leaveDays; // 연당 범위의 휴가 일수
    
    @Column(nullable = false)
    private Integer holidayDays; // 연당 휴가와는 다른 공휴일 일수 (고정)
    
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
     * 연당 근무년수가 해당 범위에 포함되는지 확인
     */
    public boolean isInRange(int workYears) {
        return workYears >= minYears && workYears <= maxYears;
    }
    
    /**
     * 범위 설명 반환
     */
    public String getRangeDescription() {
        if (minYears == maxYears) {
            return minYears + "년차";
        } else if (maxYears == 99) {
            return minYears + "년차 이상";
        } else {
            return minYears + "~" + maxYears + "년차";
        }
    }
} 
