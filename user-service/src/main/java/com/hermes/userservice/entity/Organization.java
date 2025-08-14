package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization")
@Getter
@Setter
public class Organization  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organizationId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Organization parent;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "leader_id")
    private User leader;

    // private String description;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeAssignment> assignments = new ArrayList<>();
}

