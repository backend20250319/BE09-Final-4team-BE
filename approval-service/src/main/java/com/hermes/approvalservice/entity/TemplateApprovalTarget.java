package com.hermes.approvalservice.entity;

import com.hermes.approvalservice.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_approval_target")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateApprovalTarget {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private DocumentTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_stage_id")
    private TemplateApprovalStage approvalStage;
}