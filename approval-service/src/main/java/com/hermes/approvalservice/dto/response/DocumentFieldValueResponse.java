package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.enums.FieldType;
import lombok.Data;

@Data
public class DocumentFieldValueResponse {
    
    private Long id;
    private String fieldName;
    private FieldType fieldType;
    private String fieldValue;
}