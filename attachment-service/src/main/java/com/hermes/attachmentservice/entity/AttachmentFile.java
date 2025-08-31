package com.hermes.attachmentservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "attachment_files")
public class AttachmentFile {
    
    @Id
    private String fileId; // AttachmentInfo랑 매핑용 uuid

    @Column(nullable = false)
    private String storedName; // 실제 저장 이름
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Long uploadedBy;

}