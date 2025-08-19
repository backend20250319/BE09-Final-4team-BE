package com.hermes.userservice.entity;

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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // 유저 아이디 (PK)

    @Column(nullable = false, length = 100)
    private String name;  // 이름

    @Column(nullable = false, unique = true, length = 100)
    private String email;  // 이메일

    @Column(nullable = false, length = 255)
    private String password; // 패스워드(해시)

    @Column(nullable = false, length = 100)
    private String phone; // 휴대폰 번호

    @Column(nullable = false, length = 100)
    private String address; // 거주지

    @Column(nullable = false)
    private LocalDate joinDate; // 입사일

    @Column (nullable = false)
    private Boolean isAdmin;  // 관리자 여부
    
    @Column
    private Boolean needsPasswordReset = false;  // 비밀번호 재설정 필요 여부
    
    @Column(nullable = false)
    private Boolean isLocked = false;  // 계정 잠금 여부
    
    @Column(nullable = false)
    private Integer loginAttempts = 0;  // 로그인 시도 횟수
    
    @Column
    private LocalDateTime lockedAt;  // 계정 잠금 시간
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_type_wid")
    private EmploymentType employmentType;  // 고용형태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_id")
    private Rank rank;  // 직급

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;  // 직위

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;  // 직책

    @Column(length = 100)
    private String role;  // 직무 (직접 입력)

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeAssignment> assignments = new ArrayList<>();  // 부서 배치 목록
    
    @Column
    private LocalDateTime lastLoginAt;  // 마지막 로그인 시간
    
    @Column
    private LocalDateTime createdAt;  // 생성 시간
    
    @Column
    private LocalDateTime updatedAt;  // 수정 시간
    
    @Column(length = 500)
    private String profileImage;  // 프로필 이미지 URL
    
    @Column(columnDefinition = "TEXT")
    private String selfIntroduction;  // 자기소개
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WorkHourPolicy workHourPolicy;  // 근무 정책
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeaveRecord> leaveRecords = new ArrayList<>();  // 휴가 이력
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkSchedule> workSchedules = new ArrayList<>();  // 근무 일정
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DefaultWorkSchedule defaultWorkSchedule;  // 기본 근무 일정
    
    @ManyToMany(mappedBy = "recipients", fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();  // 알림 목록
    
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
    
    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }
    
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }
    
    public void lockAccount() {
        this.isLocked = true; 
        this.lockedAt = LocalDateTime.now();
    }
    
    public void unlockAccount() {
        this.isLocked = false;
        this.lockedAt = null;
        this.loginAttempts = 0;
    }
    
    public boolean isAccountLocked() {
        return this.isLocked;
    }
    
}
