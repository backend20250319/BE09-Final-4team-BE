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
    private Long userId;   // 유저 아이디 (PK)

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
    private LocalDate hireDate; // 입사일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuthorityLevel authorityLevel; // 권한 수준 (ex: ADMIN, USER 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_type_wid")
    private EmploymentType employmentType;  // 고용형태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_id")
    private Rank rank;  // 직급

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;  // 직레벨

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;  // 직위

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_id")
    private Duty duty;  // 직책

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeAssignment> assignments = new ArrayList<>();  // 부서 배치 이력

    @Column(nullable = false)
    private Boolean isActive = true;  // 계정 활성 상태
    
    @Column(nullable = false)
    private Integer loginAttempts = 0;  // 로그인 시도 횟수
    
    @Column
    private LocalDateTime lastLoginAt;  // 마지막 로그인 시간
    
    @Column
    private LocalDateTime lockedAt;  // 계정 잠금 시간
    
    @Column
    private LocalDateTime createdAt;  // 생성 시간
    
    @Column
    private LocalDateTime updatedAt;  // 수정 시간
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }
    
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
    }
    
    public void lockAccount() {
        this.isActive = false;
        this.lockedAt = LocalDateTime.now();
    }
    
    public void unlockAccount() {
        this.isActive = true;
        this.lockedAt = null;
        this.loginAttempts = 0;
    }
    
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    public boolean isAccountLocked() {
        return this.lockedAt != null;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
}
