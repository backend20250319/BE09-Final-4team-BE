package com.hermes.approvalservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentFieldValueRequest {
    
    @NotBlank(message = "필드 이름은 필수입니다")
    private String fieldName;
    
    private String fieldValue;
    
    private Long templateFieldId;
}