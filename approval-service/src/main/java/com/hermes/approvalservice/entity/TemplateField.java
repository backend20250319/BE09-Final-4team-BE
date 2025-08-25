package com.hermes.approvalservice.entity;

import com.hermes.approvalservice.enums.FieldType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(nullable = false)
    private Integer fieldOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String options;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private DocumentTemplate template;
}