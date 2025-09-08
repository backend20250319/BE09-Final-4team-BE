package com.hermes.approvalservice.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BulkCategoryResponse {
    
    private int totalOperations;
    private int successfulOperations;
    private int failedOperations;
    private List<CategoryOperationResult> results;
    
    @Data
    public static class CategoryOperationResult {
        private String operationType;
        private Long categoryId;
        private CategoryResponse category;
        private boolean success;
        private String errorMessage;
    }
}