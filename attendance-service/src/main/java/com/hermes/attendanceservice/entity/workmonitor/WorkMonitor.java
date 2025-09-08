package com.hermes.attendanceservice.entity.workmonitor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "work_monitor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkMonitor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date; // 조회 날짜
    
    @Column(name = "total_employees", nullable = false)
    private Integer totalEmployees; // 전체 직원 수
    
    @Column(name = "attendance_count", nullable = false)
    @Builder.Default
    private Integer attendanceCount = 0; // 출석 수 (정상 출근, 재택, 출장, 외근)
    
    @Column(name = "late_count", nullable = false)
    @Builder.Default
    private Integer lateCount = 0; // 지각 수
    
    @Column(name = "vacation_count", nullable = false)
    @Builder.Default
    private Integer vacationCount = 0; // 휴가 수

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
} 