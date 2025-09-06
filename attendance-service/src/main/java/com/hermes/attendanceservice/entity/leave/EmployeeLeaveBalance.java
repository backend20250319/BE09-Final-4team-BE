package com.hermes.attendanceservice.entity.leave;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "employee_leave_balance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLeaveBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId; // 직원 ID
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType; // 연차 종류 (기본연차, 보상연차, 특별연차)
    
    @Column(name = "total_leave_days", nullable = false)
    private Integer totalLeaveDays; // 총 부여된 연차 일수
    
    @Column(name = "used_leave_days", nullable = false)
    @Builder.Default
    private Integer usedLeaveDays = 0; // 사용한 연차 일수
    
    @Column(name = "remaining_days", nullable = false)
    private Integer remainingDays; // 잔여 연차 일수
    
    @Column(name = "work_years", nullable = false)
    private Integer workYears; // 근무년수 (연차 부여 시점의 년차)
    
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
     * 연차 사용 (차감)
     */
    public void useLeave(Integer days) {
        if (days <= 0) {
            throw new IllegalArgumentException("사용할 연차 일수는 0보다 커야 합니다.");
        }
        if (days > remainingDays) {
            throw new IllegalArgumentException("잔여 연차가 부족합니다. 잔여: " + remainingDays + "일, 요청: " + days + "일");
        }
        
        this.usedLeaveDays += days;
        this.remainingDays -= days;
    }
    
    /**
     * 연차 복구 (취소 시)
     */
    public void restoreLeave(Integer days) {
        if (days <= 0) {
            throw new IllegalArgumentException("복구할 연차 일수는 0보다 커야 합니다.");
        }
        if (usedLeaveDays < days) {
            throw new IllegalArgumentException("복구할 수 없습니다. 사용한 연차: " + usedLeaveDays + "일, 복구 요청: " + days + "일");
        }
        
        this.usedLeaveDays -= days;
        this.remainingDays += days;
    }
    
    /**
     * 연차 사용률 계산 (0.0 ~ 1.0)
     */
    public double getUsageRate() {
        if (totalLeaveDays == 0) return 0.0;
        return (double) usedLeaveDays / totalLeaveDays;
    }
} 