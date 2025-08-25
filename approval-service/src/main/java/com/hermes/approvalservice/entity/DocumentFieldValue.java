package com.hermes.approvalservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_field_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String fieldValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private ApprovalDocument document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_field_id")
    private TemplateField templateField;
}