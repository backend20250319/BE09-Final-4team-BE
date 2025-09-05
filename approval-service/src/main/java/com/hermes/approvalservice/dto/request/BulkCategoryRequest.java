package com.hermes.approvalservice.dto.request;

import com.hermes.approvalservice.validation.ValidBulkCategoryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@ValidBulkCategoryRequest
public class BulkCategoryRequest {
    
    @NotEmpty(message = "작업 목록은 비어있을 수 없습니다")
    @Valid
    private List<BulkCategoryOperation> operations;
}