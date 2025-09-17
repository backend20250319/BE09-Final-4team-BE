package com.hermes.tenantservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 테넌트 엔티티
 * Schema-per-tenant 방식에서 각 테넌트의 메타데이터를 관리
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // surrogate key (내부용)

    /**
     * 테넌트 식별자 (고유한 테넌트 ID)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String tenantId; // natural key (외부용)

    /**
     * 테넌트명
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 테넌트 설명
     */
    @Column(length = 500)
    private String description;


    /**
     * 테넌트 상태 (ACTIVE, INACTIVE, SUSPENDED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;

    /**
     * 생성 시간
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 테넌트 관리자 이메일
     */
    @Column(length = 100)
    private String adminEmail;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Tenant(String tenantId, String name) {
        this.tenantId = tenantId;
        this.name = name;
        this.status = TenantStatus.ACTIVE;
    }

    /**
     * 테넌트 상태 enum
     */
    public enum TenantStatus {
        ACTIVE,    // 활성
        INACTIVE,  // 비활성
        SUSPENDED  // 일시 중단
    }
}