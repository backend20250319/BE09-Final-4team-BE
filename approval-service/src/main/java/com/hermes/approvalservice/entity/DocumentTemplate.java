package com.hermes.approvalservice.entity;

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
    private Boolean useBody = true;

    @Column(nullable = false)
    private Boolean useAttachment = false;

    @Column(nullable = false)
    private Boolean allowApprovalChange = false;

    @Column(nullable = false)
    private Boolean allowReferenceChange = false;

    @Column(nullable = false)
    private Boolean isHidden = false;

    @Column(length = 1000)
    private String referenceFiles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TemplateCategory category;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TemplateField> fields = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stageOrder ASC")
    private List<TemplateApprovalStage> approvalStages = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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