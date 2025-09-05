package com.hermes.approvalservice.dto.request;

import com.hermes.approvalservice.enums.CategoryOperationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkCategoryOperation {
    
    @NotNull(message = "작업 타입은 필수입니다")
    private CategoryOperationType type;
    
    private Long id; // UPDATE, DELETE 작업에만 필요
    
    @Valid
    private CreateCategoryRequest createRequest; // CREATE 작업에만 필요
    
    @Valid
    private UpdateCategoryRequest updateRequest; // UPDATE 작업에만 필요
}