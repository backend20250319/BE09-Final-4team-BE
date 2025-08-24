package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponse {
    
    private Long id;
    private String name;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}