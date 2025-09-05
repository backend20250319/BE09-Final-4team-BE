package com.hermes.approvalservice.entity;

import com.hermes.approvalservice.enums.AttachmentUsageType;
import com.hermes.attachment.entity.AttachmentInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String icon;

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean useBody = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AttachmentUsageType useAttachment = AttachmentUsageType.OPTIONAL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowTargetChange = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isHidden = false;

    @ElementCollection
    @CollectionTable(name = "template_reference_files", joinColumns = @JoinColumn(name = "template_id"))
    @Builder.Default
    private List<AttachmentInfo> referenceFiles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TemplateCategory category;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TemplateField> fields = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stageOrder ASC")
    @Builder.Default
    private List<TemplateApprovalStage> approvalStages = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TemplateApprovalTarget> referenceTargets = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}