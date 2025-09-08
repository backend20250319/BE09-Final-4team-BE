package com.hermes.approvalservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentFieldValueRequest {
    
    @NotNull(message = "템플릿 필드 ID는 필수입니다")
    private Long templateFieldId;
    
    private String fieldValue;
}