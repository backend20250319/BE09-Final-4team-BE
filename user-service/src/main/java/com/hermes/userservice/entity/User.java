package com.hermes.userservice.entity;

import com.hermes.userservice.listener.PasswordEncryptionListener;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(PasswordEncryptionListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // User ID (PK)

    @Column(nullable = false, length = 100)
    private String name;  // Name

    @Column(nullable = false, unique = true, length = 100)
    private String email;  // Email

    @Column(nullable = false, length = 255)
    private String password; // Password (hashed)

    @Column(nullable = false, length = 100)
    private String phone; // Phone number

    @Column(nullable = false, length = 100)
    private String address; // Address

    @Column(nullable = false)
    private LocalDate joinDate = LocalDate.now(); // Join date

    @Column (nullable = false)
    private Boolean isAdmin = false;  // Admin flag

    @Column
    private Boolean needsPasswordReset = false;  // Password reset required flag

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_type_wid")
    private EmploymentType employmentType;  // Employment type

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_id")
    private Rank rank;  // Rank

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;  // Position

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;  // Job

    @Column(length = 100)
    private String role;  // Role (direct input)

    @Column
    private LocalDateTime lastLoginAt;  // Last login time

    @Column
    private LocalDateTime createdAt;  // Created time

    @Column
    private LocalDateTime updatedAt;  // Updated time

    @Column(length = 500)
    private String profileImage;  // Profile image URL

    // @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private WorkHourPolicy workHourPolicy;  // 근무 정책

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<LeaveRecord> leaveRecords = new ArrayList<>();  // 휴가 이력

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<WorkSchedule> workSchedules = new ArrayList<>();  // 근무 일정

    // @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private DefaultWorkSchedule defaultWorkSchedule;  // 기본 근무 일정

    // @ManyToMany(mappedBy = "recipients", fetch = FetchType.LAZY)
    // private List<Notification> notifications = new ArrayList<>();  // 알림 목록

    @Column(columnDefinition = "TEXT")
    private String selfIntroduction;  // Self introduction

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

}