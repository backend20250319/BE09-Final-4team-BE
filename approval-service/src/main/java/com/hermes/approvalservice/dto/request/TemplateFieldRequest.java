package com.hermes.approvalservice.dto.request;

import com.hermes.approvalservice.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemplateFieldRequest {
    
    @NotBlank(message = "필드 이름은 필수입니다")
    private String name;
    
    @NotNull(message = "필드 타입은 필수입니다")
    private FieldType fieldType;
    
    @NotNull(message = "필수 여부는 필수입니다")
    private Boolean required;
    
    @NotNull(message = "필드 순서는 필수입니다")
    private Integer fieldOrder;
    
    private String options;
}