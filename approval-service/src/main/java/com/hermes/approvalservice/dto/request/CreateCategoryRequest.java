package com.hermes.approvalservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    
    @NotBlank(message = "카테고리 이름은 필수입니다")
    private String name;
    
    @NotNull(message = "정렬 순서는 필수입니다")
    private Integer sortOrder;
}