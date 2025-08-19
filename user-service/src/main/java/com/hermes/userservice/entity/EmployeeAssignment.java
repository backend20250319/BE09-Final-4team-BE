package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_assignment")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private Boolean isPrimary = false;  // 메인부서 여부

    @Column(nullable = false)
    private Boolean isLeader = false;  // 해당 부서에서의 조직장 여부
}

