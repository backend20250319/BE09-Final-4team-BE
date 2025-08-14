package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employment_type")
@Getter
@Setter
public class EmploymentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;  // 예: 정규직, 계약직

    private String description;
}

