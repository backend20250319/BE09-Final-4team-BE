package com.hermes.approvalservice.dto.request;

import com.hermes.approvalservice.enums.CategoryOperationType;
import com.hermes.approvalservice.validation.ValidCategoryOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ValidCategoryOperation
public class BulkCategoryOperation {
    
    @NotNull(message = "작업 타입은 필수입니다")
    private CategoryOperationType type;
    
    @Schema(description = "카테고리 ID (UPDATE, DELETE 작업시 필요)")
    private Long id; // UPDATE, DELETE 작업에만 필요
    
    @Valid
    @Schema(description = "카테고리 생성 요청 (CREATE 작업시 필요)")
    private CreateCategoryRequest createRequest; // CREATE 작업에만 필요
    
    @Valid
    @Schema(description = "카테고리 수정 요청 (UPDATE 작업시 필요)")
    private UpdateCategoryRequest updateRequest; // UPDATE 작업에만 필요
}