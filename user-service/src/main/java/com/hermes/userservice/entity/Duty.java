package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "duties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Duty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
