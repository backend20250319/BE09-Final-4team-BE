package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_change_history")
@Getter
@Setter
public class EmployeeInfoChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeInfoChangeType  employeeInfoChangeType ;

    @Column(length = 100)
    private String beforeValue;

    @Column(length = 100)
    private String afterValue;

    @Column
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }
}

