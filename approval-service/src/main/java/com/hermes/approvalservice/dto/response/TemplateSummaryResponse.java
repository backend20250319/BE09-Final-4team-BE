package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.enums.AttachmentUsageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TemplateSummaryResponse {
    
    private Long id;
    private String title;
    private String icon;
    private String description;
    private Boolean useBody;
    private AttachmentUsageType useAttachment;
    private Boolean allowTargetChange;
    private Boolean isHidden;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}