package com.hermes.workscheduleservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_time_adjustments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WorkTimeAdjustment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private LocalDate adjustDate;
    
    @Column(nullable = false, length = 20)
    private String adjustType; // EXTEND, REDUCE, FLEXIBLE
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false, length = 200)
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Boolean isApproved = false;
    
    @Column(length = 50)
    private String approverId;
    
    @Column(columnDefinition = "TEXT")
    private String approverComment;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, CANCELLED
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 승인 처리
    public void approve(String approverId, String comment) {
        this.isApproved = true;
        this.approverId = approverId;
        this.approverComment = comment;
        this.status = "APPROVED";
        this.updatedAt = LocalDateTime.now();
    }
    
    // 거절 처리
    public void reject(String approverId, String comment) {
        this.isApproved = false;
        this.approverId = approverId;
        this.approverComment = comment;
        this.status = "REJECTED";
        this.updatedAt = LocalDateTime.now();
    }
    
    // 취소 처리
    public void cancel() {
        this.status = "CANCELLED";
        this.updatedAt = LocalDateTime.now();
    }
    
    // 근무 시간 계산 (분 단위)
    public int getWorkMinutes() {
        int startMinutes = startTime.getHour() * 60 + startTime.getMinute();
        int endMinutes = endTime.getHour() * 60 + endTime.getMinute();
        return endMinutes - startMinutes;
    }
    
    // 근무 시간 계산 (시간 단위)
    public double getWorkHours() {
        return getWorkMinutes() / 60.0;
    }
} 