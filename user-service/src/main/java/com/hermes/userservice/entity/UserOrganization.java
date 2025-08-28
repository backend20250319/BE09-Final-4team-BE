package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_organizations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserOrganization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "organization_name", nullable = false, length = 100)
    private String organizationName;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;
    
    @Column(name = "is_leader", nullable = false)
    private Boolean isLeader;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    public void setUser(User user) {
        this.user = user;
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.assignedAt == null) {
            this.assignedAt = LocalDateTime.now();
        }
    }
}