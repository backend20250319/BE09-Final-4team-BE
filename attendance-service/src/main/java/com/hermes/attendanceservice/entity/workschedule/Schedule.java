package com.hermes.attendanceservice.entity.workschedule;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.util.List;
import com.hermes.attendanceservice.entity.workschedule.ScheduleType;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Schedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ScheduleType scheduleType; // WORK(근무), SICK_LEAVE(병가), VACATION(휴가), BUSINESS_TRIP(출장), OUT_OF_OFFICE(외근), OVERTIME(초과근무)
    
    @Column(length = 20)
    private String color;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAllDay = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;
    
    @Column(length = 20)
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY, YEARLY
    
    @Builder.Default
    private Integer recurrenceInterval = 1;
    
    @ElementCollection
    @CollectionTable(name = "schedule_recurrence_days", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "day_of_week", length = 20)
    private List<String> recurrenceDays; // MONDAY, TUESDAY, etc.
    
    private LocalDate recurrenceEndDate;
    
    private Long workPolicyId;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 5;
    
    @Column(length = 200)
    private String location;
    
    @ElementCollection
    @CollectionTable(name = "schedule_attendees", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "attendee_email", length = 100)
    private List<String> attendees;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isFixed = false; // 고정 스케줄 여부
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEditable = true; // 편집 가능 여부
    
    @Column(length = 50)
    private String fixedReason; // 고정 사유 (WORK_POLICY, HOLIDAY, BREAK_TIME)
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, CANCELLED, COMPLETED
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // 스케줄 상태 변경 메서드
    public void cancel() {
        this.status = "CANCELLED";
        this.updatedAt = Instant.now();
    }
    
    public void complete() {
        this.status = "COMPLETED";
        this.updatedAt = Instant.now();
    }
    
    public void activate() {
        this.status = "ACTIVE";
        this.updatedAt = Instant.now();
    }
} 