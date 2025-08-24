package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.enums.FieldType;
import lombok.Data;

@Data
public class TemplateFieldResponse {
    
    private Long id;
    private String name;
    private FieldType fieldType;
    private Boolean required;
    private Integer fieldOrder;
    private String options;
}