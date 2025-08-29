package com.hermes.attachment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentInfo {

    @Column(nullable = false, length = 100)
    private String fileId;

    @Column(nullable = false, length = 255)
    private String displayFileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 100)
    private String contentType;
}