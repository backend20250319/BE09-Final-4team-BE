package com.hermes.approvalservice.converter;

import com.hermes.api.common.ApiResult;
import com.hermes.attachment.service.AttachmentClientService;
import com.hermes.approvalservice.client.UserServiceClient;
import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.dto.response.*;
import com.hermes.approvalservice.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseConverter {

    private final UserServiceClient userServiceClient;
    private final AttachmentClientService attachmentClientService;

    public DocumentActivityResponse convertToDocumentActivityResponse(DocumentActivity activity) {
        DocumentActivityResponse response = new DocumentActivityResponse();
        response.setId(activity.getId());
        response.setActivityType(activity.getActivityType());
        
        ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(activity.getUserId());
        response.setUser(userResult.getData());
        
        response.setDescription(activity.getDescription());
        response.setReason(activity.getReason());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }

    public DocumentCommentResponse convertToDocumentCommentResponse(DocumentComment comment) {
        DocumentCommentResponse response = new DocumentCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        
        ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(comment.getAuthorId());
        response.setAuthor(userResult.getData());
        
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    public TemplateFieldResponse convertToTemplateFieldResponse(TemplateField field) {
        TemplateFieldResponse response = new TemplateFieldResponse();
        response.setId(field.getId());
        response.setName(field.getName());
        response.setFieldType(field.getFieldType());
        response.setRequired(field.getRequired());
        response.setFieldOrder(field.getFieldOrder());
        response.setOptions(field.getOptions());
        return response;
    }

    public DocumentFieldValueResponse convertToDocumentFieldValueResponse(DocumentFieldValue fieldValue) {
        DocumentFieldValueResponse response = new DocumentFieldValueResponse();
        response.setId(fieldValue.getId());
        response.setFieldName(fieldValue.getFieldName());
        response.setFieldValue(fieldValue.getFieldValue());
        
        if (fieldValue.getTemplateField() != null) {
            response.setTemplateField(convertToTemplateFieldResponse(fieldValue.getTemplateField()));
        }
        
        return response;
    }

    public ApprovalTargetResponse convertToApprovalTargetResponse(DocumentApprovalTarget target) {
        ApprovalTargetResponse response = new ApprovalTargetResponse();
        response.setId(target.getId());
        response.setTargetType(target.getTargetType());
        response.setOrganizationId(target.getOrganizationId());
        response.setManagerLevel(target.getManagerLevel());
        response.setIsReference(target.getIsReference());
        response.setIsApproved(target.getIsApproved());
        response.setApprovedAt(target.getApprovedAt());
        
        if (target.getUserId() != null) {
            ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(target.getUserId());
            response.setUser(userResult.getData());
        }
        
        if (target.getApprovedBy() != null) {
            ApiResult<UserProfile> approverResult = userServiceClient.getUserProfile(target.getApprovedBy());
            response.setApprover(approverResult.getData());
        }
        
        return response;
    }

    public ApprovalTargetResponse convertToApprovalTargetResponse(TemplateApprovalTarget target) {
        ApprovalTargetResponse response = new ApprovalTargetResponse();
        response.setId(target.getId());
        response.setTargetType(target.getTargetType());
        response.setOrganizationId(target.getOrganizationId());
        response.setManagerLevel(target.getManagerLevel());
        response.setIsReference(target.getIsReference());
        response.setIsApproved(false); // 템플릿은 승인 상태 없음
        response.setApprovedAt(null);
        
        if (target.getUserId() != null) {
            ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(target.getUserId());
            response.setUser(userResult.getData());
        }
        
        return response;
    }

    public ApprovalStageResponse convertToApprovalStageResponse(DocumentApprovalStage stage) {
        ApprovalStageResponse response = new ApprovalStageResponse();
        response.setId(stage.getId());
        response.setStageOrder(stage.getStageOrder());
        response.setStageName(stage.getStageName());
        response.setIsCompleted(stage.getIsCompleted());
        response.setCompletedAt(stage.getCompletedAt());
        
        response.setApprovalTargets(stage.getApprovalTargets().stream()
                .map(this::convertToApprovalTargetResponse)
                .toList());
        
        return response;
    }

    public ApprovalStageResponse convertToApprovalStageResponse(TemplateApprovalStage stage) {
        ApprovalStageResponse response = new ApprovalStageResponse();
        response.setId(stage.getId());
        response.setStageOrder(stage.getStageOrder());
        response.setStageName(stage.getStageName());
        response.setIsCompleted(false); // 템플릿은 완료 상태 없음
        response.setCompletedAt(null);
        
        response.setApprovalTargets(stage.getApprovalTargets().stream()
                .map(this::convertToApprovalTargetResponse)
                .toList());
        
        return response;
    }

    public TemplateResponse convertToTemplateResponse(DocumentTemplate template) {
        TemplateResponse response = new TemplateResponse();
        response.setId(template.getId());
        response.setTitle(template.getTitle());
        response.setIcon(template.getIcon());
        response.setDescription(template.getDescription());
        response.setBodyTemplate(template.getBodyTemplate());
        response.setUseBody(template.getUseBody());
        response.setUseAttachment(template.getUseAttachment());
        response.setAllowTargetChange(template.getAllowTargetChange());
        response.setIsHidden(template.getIsHidden());
        response.setReferenceFiles(attachmentClientService.convertToResponseList(template.getReferenceFiles()));
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());

        if (template.getCategory() != null) {
            CategoryResponse categoryResponse = new CategoryResponse();
            categoryResponse.setId(template.getCategory().getId());
            categoryResponse.setName(template.getCategory().getName());
            categoryResponse.setDescription(template.getCategory().getDescription());
            categoryResponse.setSortOrder(template.getCategory().getSortOrder());
            response.setCategory(categoryResponse);
        }

        response.setFields(template.getFields().stream()
                .map(this::convertToTemplateFieldResponse)
                .toList());

        response.setApprovalStages(template.getApprovalStages().stream()
                .map(this::convertToApprovalStageResponse)
                .toList());

        response.setReferenceTargets(template.getReferenceTargets().stream()
                .map(this::convertToApprovalTargetResponse)
                .toList());

        return response;
    }
}