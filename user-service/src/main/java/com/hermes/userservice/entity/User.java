package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    private static final Logger log = LoggerFactory.getLogger(User.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String phone;

    @Column(length = 100)
    private String address;

    @Column(nullable = false)
    private LocalDate joinDate;

    @Column(name = "work_years")
    private Integer workYears;

    @Column(nullable = false)
    private Boolean isAdmin;

    @Column
    private Boolean needsPasswordReset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_type_wid")
    private EmploymentType employmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_id")
    private Rank rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(length = 100)
    private String role;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(name = "work_policy_id")
    private Long workPolicyId;

    @Column(columnDefinition = "TEXT")
    private String selfIntroduction;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserOrganization> userOrganizations = new ArrayList<>();

    public void updateInfo(String name, String phone, String address, String profileImageUrl, String selfIntroduction) {
        log.info("updateInfo 호출: name={}, phone={}, address={}", name, phone, address);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.selfIntroduction = selfIntroduction;
        log.info("updateInfo 완료: this.name={}, this.phone={}, this.address={}", this.name, this.phone, this.address);
    }

    public void updateJoinDate(LocalDate joinDate) {
        log.info("updateJoinDate 호출: joinDate={}", joinDate);
        this.joinDate = joinDate;
        log.info("updateJoinDate 완료: this.joinDate={}", this.joinDate);
    }

    public void updateWorkYears(Integer workYears) {
        log.info("updateWorkYears 호출: workYears={}", workYears);
        this.workYears = workYears;
        log.info("updateWorkYears 완료: this.workYears={}", this.workYears);
    }

    public void updateWorkInfo(EmploymentType employmentType, Rank rank, Position position, Job job, String role, Long workPolicyId) {
        this.employmentType = employmentType;
        this.rank = rank;
        this.position = position;
        this.job = job;
        this.role = role;
        this.workPolicyId = workPolicyId;
    }
    
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updatePassword(String newHashedPassword) {
        this.password = newHashedPassword;
        this.needsPasswordReset = false;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updateAdminStatus(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void updatePasswordResetFlag(Boolean needsPasswordReset) {
        this.needsPasswordReset = needsPasswordReset;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateSelfIntroduction(String selfIntroduction) {
        this.selfIntroduction = selfIntroduction;
    }

    public void updateEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public void updateRank(Rank rank) {
        this.rank = rank;
    }

    public void updatePosition(Position position) {
        this.position = position;
    }

    public void updateJob(Job job) {
        this.job = job;
    }

    public void updateRole(String role) {
        this.role = role;
    }

    public void updateWorkPolicyId(Long workPolicyId) {
        this.workPolicyId = workPolicyId;
    }

    public void updateUserOrganizations(List<UserOrganization> organizations) {
        this.userOrganizations.clear();
        if (organizations != null) {
            this.userOrganizations.addAll(organizations);
            organizations.forEach(org -> org.setUser(this));
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.joinDate == null) {
            this.joinDate = LocalDate.now();
        }
        if (this.isAdmin == null) {
            this.isAdmin = false;
        }
        if (this.needsPasswordReset == null) {
            this.needsPasswordReset = false;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}