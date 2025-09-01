package com.hermes.approvalservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_approval_stage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentApprovalStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer stageOrder;

    @Column(nullable = false, length = 100)
    private String stageName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private ApprovalDocument document;

    @OneToMany(mappedBy = "approvalStage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DocumentApprovalTarget> approvalTargets = new ArrayList<>();
}