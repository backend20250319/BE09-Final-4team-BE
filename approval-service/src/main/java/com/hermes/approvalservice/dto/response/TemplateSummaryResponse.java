package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TemplateSummaryResponse {
    
    private Long id;
    private String title;
    private String icon;
    private String description;
    private Boolean useBody;
    private Boolean useAttachment;
    private Boolean allowTargetChange;
    private Boolean isHidden;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}