package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TemplatesByCategoryResponse {
    
    private Long categoryId;
    private String categoryName;
    private List<TemplateSummaryResponse> templates;
}