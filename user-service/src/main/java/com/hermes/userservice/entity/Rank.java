package com.hermes.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ranks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
