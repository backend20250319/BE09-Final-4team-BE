package com.hermes.approvalservice.dto.response;

import com.hermes.attachment.dto.AttachmentInfoResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentResponse extends BaseDocumentResponse {
    
    private TemplateResponse template;
    private List<DocumentFieldValueResponse> fieldValues;
    private List<ApprovalStageResponse> approvalStages;
    private List<ApprovalTargetResponse> referenceTargets;
    private List<DocumentActivityResponse> activities;
    private List<DocumentCommentResponse> comments;
    private List<AttachmentInfoResponse> attachments;
}