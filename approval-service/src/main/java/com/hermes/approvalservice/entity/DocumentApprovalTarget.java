package com.hermes.approvalservice.entity;

import com.hermes.approvalservice.enums.ApprovalStatus;
import com.hermes.approvalservice.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_approval_target")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentApprovalTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @Column
    private Long userId;

    @Column
    private Long organizationId;

    @Column
    private Integer managerLevel;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isReference = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column
    private Long processedBy;

    @Column
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private ApprovalDocument document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_stage_id")
    private DocumentApprovalStage approvalStage;
}