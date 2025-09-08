package com.hermes.approvalservice.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentSummaryResponse extends BaseDocumentResponse {
    
    private TemplateSummaryResponse template;
    private Integer totalStages;
}