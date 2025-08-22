package com.hermes.approvalservice.dto.response;

import lombok.Data;

@Data
public class DocumentFieldValueResponse {
    
    private Long id;
    private String fieldName;
    private String fieldValue;
    private TemplateFieldResponse templateField;
}