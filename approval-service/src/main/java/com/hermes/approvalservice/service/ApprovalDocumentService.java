package com.hermes.approvalservice.service;

import com.hermes.api.common.ApiResult;
import com.hermes.attachment.entity.AttachmentInfo;
import com.hermes.attachment.service.AttachmentClientService;
import com.hermes.approvalservice.client.UserServiceClient;
import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.dto.request.CreateDocumentRequest;
import com.hermes.approvalservice.dto.request.UpdateDocumentRequest;
import com.hermes.approvalservice.dto.response.*;
import com.hermes.approvalservice.entity.*;
import com.hermes.approvalservice.enums.ActivityType;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.exception.UnauthorizedException;
import com.hermes.approvalservice.repository.*;
import com.hermes.auth.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalDocumentService {

    private final ApprovalDocumentRepository documentRepository;
    private final DocumentTemplateRepository templateRepository;
    private final DocumentPermissionService permissionService;
    private final DocumentActivityService activityService;
    private final AttachmentClientService attachmentService;
    private final UserServiceClient userServiceClient;


    public Page<DocumentSummaryResponse> getDocumentsForUser(UserPrincipal user, 
                                                            List<DocumentStatus> statuses, String search, 
                                                            LocalDate startDate, LocalDate endDate, 
                                                            Pageable pageable) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        Long userId = user.getId();
        
        return documentRepository.findDocumentsForUserWithFilters(userId, statuses, search, 
                                                                 startDateTime, endDateTime, pageable)
                .map(document -> convertToSummaryResponse(document, user));
    }


    public DocumentResponse getDocumentById(Long id, UserPrincipal user) {
        ApprovalDocument document = documentRepository.findByIdWithDetails(id);
        if (document == null) {
            throw new NotFoundException("문서를 찾을 수 없습니다.");
        }

        if (!permissionService.canViewDocument(document, user)) {
            throw new UnauthorizedException("문서 조회 권한이 없습니다.");
        }

        return convertToResponse(document, user);
    }

    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request, UserPrincipal author) {
        DocumentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다."));

        Long authorId = author.getId();

        // TODO: 템플릿의 각종 옵션들을 기반으로 request 검증

        // 첨부파일 검증 및 변환
        List<AttachmentInfo> attachments = attachmentService.validateAndConvertAttachments(request.getAttachments());

        ApprovalDocument document = ApprovalDocument.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .status(DocumentStatus.DRAFT)
                .authorId(authorId)
                .currentStage(0)
                .template(template)
                .attachments(attachments)
                .build();

        ApprovalDocument savedDocument = documentRepository.save(document);

        // Save field values, approval stages, and reference targets
        // (Implementation details)

        activityService.recordActivity(savedDocument, authorId, ActivityType.CREATE, "문서를 작성했습니다.");

        return convertToResponse(savedDocument, author);
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, UpdateDocumentRequest request, UserPrincipal user) {
        ApprovalDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        Long userId = user.getId();

        if (!permissionService.canEditDocument(document, user)) {
            throw new UnauthorizedException("문서 수정 권한이 없습니다.");
        }

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new UnauthorizedException("임시저장 상태의 문서만 수정할 수 있습니다.");
        }

        // TODO: 템플릿의 각종 옵션들을 기반으로 request 검증

        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        
        // 첨부파일 업데이트
        if (request.getAttachments() != null) {
            List<AttachmentInfo> attachments = attachmentService.validateAndConvertAttachments(request.getAttachments());
            document.getAttachments().clear();
            document.getAttachments().addAll(attachments);
        }

        // Update field values, approval stages, and reference targets
        // (Implementation details)

        activityService.recordActivity(document, userId, ActivityType.UPDATE, "문서를 수정했습니다.");

        return convertToResponse(document, user);
    }

    @Transactional
    public void submitDocument(Long id, Long userId) {
        ApprovalDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!document.getAuthorId().equals(userId)) {
            throw new UnauthorizedException("문서 제출 권한이 없습니다.");
        }

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new UnauthorizedException("임시저장 상태의 문서만 제출할 수 있습니다.");
        }

        document.setStatus(DocumentStatus.IN_PROGRESS);
        document.setSubmittedAt(LocalDateTime.now());
        document.setCurrentStage(1);

        activityService.recordActivity(document, userId, ActivityType.SUBMIT, "결재를 요청했습니다.");
    }

    private DocumentSummaryResponse convertToSummaryResponse(ApprovalDocument document, UserPrincipal user) {
        DocumentSummaryResponse response = new DocumentSummaryResponse();
        response.setId(document.getId());
        response.setTitle(document.getTitle());
        response.setContent(document.getContent());
        response.setStatus(document.getStatus());
        
        ApiResult<UserProfile> authorResult = userServiceClient.getUserProfile(document.getAuthorId());
        response.setAuthor(authorResult.getData());
        
        response.setTemplateTitle(document.getTemplate().getTitle());
        response.setCurrentStage(document.getCurrentStage());
        response.setTotalStages(document.getApprovalStages().size());
        
        // Set user role if user information is available
        if (user != null) {
            response.setUserRole(permissionService.getUserRole(document, user));
        }
        
        response.setCreatedAt(document.getCreatedAt());
        response.setSubmittedAt(document.getSubmittedAt());
        response.setApprovedAt(document.getApprovedAt());
        return response;
    }

    private DocumentResponse convertToResponse(ApprovalDocument document, UserPrincipal user) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setTitle(document.getTitle());
        response.setContent(document.getContent());
        response.setStatus(document.getStatus());
        
        ApiResult<UserProfile> authorResult = userServiceClient.getUserProfile(document.getAuthorId());
        response.setAuthor(authorResult.getData());
        
        response.setCurrentStage(document.getCurrentStage());
        
        // Set user role if user information is available
        if (user != null) {
            response.setUserRole(permissionService.getUserRole(document, user));
        }
        
        // Template 정보 변환
        response.setTemplate(convertTemplateToResponse(document.getTemplate()));
        
        // Field values 변환
        response.setFieldValues(document.getFieldValues().stream()
                .map(this::convertDocumentFieldValueToResponse)
                .toList());
        
        // Approval stages 변환  
        response.setApprovalStages(document.getApprovalStages().stream()
                .map(this::convertApprovalStageToResponse)
                .toList());
        
        // Reference targets 변환
        response.setReferenceTargets(document.getReferenceTargets().stream()
                .map(this::convertDocumentApprovalTargetToResponse)
                .toList());
        
        // Activities 변환
        response.setActivities(document.getActivities().stream()
                .map(activity -> {
                    DocumentActivityResponse activityResponse = new DocumentActivityResponse();
                    activityResponse.setId(activity.getId());
                    activityResponse.setActivityType(activity.getActivityType());
                    
                    ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(activity.getUserId());
                    activityResponse.setUser(userResult.getData());
                    
                    activityResponse.setDescription(activity.getDescription());
                    activityResponse.setReason(activity.getReason());
                    activityResponse.setCreatedAt(activity.getCreatedAt());
                    return activityResponse;
                })
                .toList());
        
        // Comments 변환
        response.setComments(document.getComments().stream()
                .map(this::convertDocumentCommentToResponse)
                .toList());
        
        // 첨부파일 정보 변환
        response.setAttachments(attachmentService.convertToResponseList(document.getAttachments()));
        
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        response.setSubmittedAt(document.getSubmittedAt());
        response.setApprovedAt(document.getApprovedAt());
        
        return response;
    }
    
    private ApprovalTargetResponse convertDocumentApprovalTargetToResponse(DocumentApprovalTarget target) {
        ApprovalTargetResponse response = new ApprovalTargetResponse();
        response.setId(target.getId());
        response.setTargetType(target.getTargetType());
        response.setOrganizationId(target.getOrganizationId());
        response.setManagerLevel(target.getManagerLevel());
        response.setIsReference(target.getIsReference());
        response.setIsApproved(target.getIsApproved());
        response.setApprovedAt(target.getApprovedAt());
        
        // Convert userId to UserProfileInfo
        if (target.getUserId() != null) {
            ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(target.getUserId());
            response.setUser(userResult.getData());
        }
        
        // Convert approvedBy to UserProfileInfo
        if (target.getApprovedBy() != null) {
            ApiResult<UserProfile> approverResult = userServiceClient.getUserProfile(target.getApprovedBy());
            response.setApprover(approverResult.getData());
        }
        
        return response;
    }
    
    private DocumentFieldValueResponse convertDocumentFieldValueToResponse(DocumentFieldValue fieldValue) {
        DocumentFieldValueResponse response = new DocumentFieldValueResponse();
        response.setId(fieldValue.getId());
        response.setFieldName(fieldValue.getFieldName());
        response.setFieldValue(fieldValue.getFieldValue());
        
        if (fieldValue.getTemplateField() != null) {
            TemplateFieldResponse fieldResponse = new TemplateFieldResponse();
            fieldResponse.setId(fieldValue.getTemplateField().getId());
            fieldResponse.setName(fieldValue.getTemplateField().getName());
            fieldResponse.setFieldType(fieldValue.getTemplateField().getFieldType());
            fieldResponse.setRequired(fieldValue.getTemplateField().getRequired());
            fieldResponse.setFieldOrder(fieldValue.getTemplateField().getFieldOrder());
            fieldResponse.setOptions(fieldValue.getTemplateField().getOptions());
            response.setTemplateField(fieldResponse);
        }
        
        return response;
    }
    
    private ApprovalStageResponse convertApprovalStageToResponse(DocumentApprovalStage stage) {
        ApprovalStageResponse response = new ApprovalStageResponse();
        response.setId(stage.getId());
        response.setStageOrder(stage.getStageOrder());
        response.setStageName(stage.getStageName());
        response.setIsCompleted(stage.getIsCompleted());
        response.setCompletedAt(stage.getCompletedAt());
        
        // Convert approval targets
        response.setApprovalTargets(stage.getApprovalTargets().stream()
                .map(this::convertDocumentApprovalTargetToResponse)
                .toList());
        
        return response;
    }
    
    private DocumentCommentResponse convertDocumentCommentToResponse(DocumentComment comment) {
        DocumentCommentResponse response = new DocumentCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        
        ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(comment.getAuthorId());
        response.setAuthor(userResult.getData());
        
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
    
    private TemplateResponse convertTemplateToResponse(DocumentTemplate template) {
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
        response.setReferenceFiles(attachmentService.convertToResponseList(template.getReferenceFiles()));
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
                .map(field -> {
                    TemplateFieldResponse fieldResponse = new TemplateFieldResponse();
                    fieldResponse.setId(field.getId());
                    fieldResponse.setName(field.getName());
                    fieldResponse.setFieldType(field.getFieldType());
                    fieldResponse.setRequired(field.getRequired());
                    fieldResponse.setFieldOrder(field.getFieldOrder());
                    fieldResponse.setOptions(field.getOptions());
                    return fieldResponse;
                })
                .toList());

        response.setApprovalStages(template.getApprovalStages().stream()
                .map(stage -> {
                    ApprovalStageResponse stageResponse = new ApprovalStageResponse();
                    stageResponse.setId(stage.getId());
                    stageResponse.setStageOrder(stage.getStageOrder());
                    stageResponse.setStageName(stage.getStageName());
                    stageResponse.setIsCompleted(false);
                    stageResponse.setCompletedAt(null);
                    
                    stageResponse.setApprovalTargets(stage.getApprovalTargets().stream()
                            .map(target -> {
                                ApprovalTargetResponse targetResponse = new ApprovalTargetResponse();
                                targetResponse.setId(target.getId());
                                targetResponse.setTargetType(target.getTargetType());
                                targetResponse.setOrganizationId(target.getOrganizationId());
                                targetResponse.setManagerLevel(target.getManagerLevel());
                                targetResponse.setIsReference(target.getIsReference());
                                targetResponse.setIsApproved(false);
                                targetResponse.setApprovedAt(null);
                                
                                if (target.getUserId() != null) {
                                    ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(target.getUserId());
                                    targetResponse.setUser(userResult.getData());
                                }
                                
                                return targetResponse;
                            })
                            .toList());
                    
                    return stageResponse;
                })
                .toList());

        response.setReferenceTargets(template.getReferenceTargets().stream()
                .map(target -> {
                    ApprovalTargetResponse targetResponse = new ApprovalTargetResponse();
                    targetResponse.setId(target.getId());
                    targetResponse.setTargetType(target.getTargetType());
                    targetResponse.setOrganizationId(target.getOrganizationId());
                    targetResponse.setManagerLevel(target.getManagerLevel());
                    targetResponse.setIsReference(target.getIsReference());
                    targetResponse.setIsApproved(false);
                    targetResponse.setApprovedAt(null);
                    
                    if (target.getUserId() != null) {
                        ApiResult<UserProfile> userResult = userServiceClient.getUserProfile(target.getUserId());
                        targetResponse.setUser(userResult.getData());
                    }
                    
                    return targetResponse;
                })
                .toList());

        return response;
    }
}