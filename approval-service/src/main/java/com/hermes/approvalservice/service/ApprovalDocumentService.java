package com.hermes.approvalservice.service;

import com.hermes.api.common.ApiResult;
import com.hermes.approvalservice.dto.request.*;
import com.hermes.attachment.entity.AttachmentInfo;
import com.hermes.attachment.service.AttachmentClientService;
import com.hermes.approvalservice.client.UserServiceClient;
import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.converter.ResponseConverter;
import com.hermes.approvalservice.dto.response.*;
import com.hermes.approvalservice.entity.*;
import com.hermes.approvalservice.enums.ActivityType;
import com.hermes.approvalservice.enums.AttachmentUsageType;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.exception.BusinessException;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.exception.UnauthorizedException;
import com.hermes.approvalservice.repository.*;
import com.hermes.auth.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalDocumentService {

    private final ApprovalDocumentRepository documentRepository;
    private final DocumentTemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final DocumentPermissionService permissionService;
    private final DocumentActivityService activityService;
    private final AttachmentClientService attachmentService;
    private final UserServiceClient userServiceClient;
    private final ResponseConverter responseConverter;


    public Page<DocumentSummaryResponse> getDocumentsForUser(UserPrincipal user, 
                                                            List<DocumentStatus> statuses, String search, 
                                                            LocalDate startDate, LocalDate endDate, 
                                                            Pageable pageable) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        Long userId = user.getId();
        
        // 검색어가 있을 경우 작성자 이름으로도 검색
        List<Long> authorIds = null;
        if (search != null && !search.trim().isEmpty()) {
            try {
                authorIds = userServiceClient.searchUserIds(search.trim());
            } catch (Exception e) {
                log.warn("사용자 검색 중 오류 발생, 템플릿 제목으로만 검색: {}", e.getMessage());
            }
        }
        
        return documentRepository.findDocumentsForUserWithFilters(userId, statuses, search, 
                                                                 authorIds, startDateTime, endDateTime, pageable)
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
    public DocumentResponse createDocument(CreateDocumentRequest request, UserPrincipal user) {
        DocumentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다."));

        // 템플릿 옵션 검증
        validateTemplateOptions(template, request.getContent(), request.getAttachments(), request.getApprovalStages());

        // 첨부파일 검증 및 변환
        List<AttachmentInfo> attachments = attachmentService.validateAndConvertAttachments(request.getAttachments());

        ApprovalDocument document = ApprovalDocument.builder()
                .content(request.getContent())
                .status(DocumentStatus.DRAFT)
                .authorId(user.getId())
                .currentStage(0)
                .template(template)
                .attachments(attachments)
                .build();

        ApprovalDocument savedDocument = documentRepository.save(document);

        // Save related entities using helper methods
        saveFieldValues(savedDocument, request.getFieldValues());
        saveApprovalStages(savedDocument, request.getApprovalStages());
        saveReferenceTargets(savedDocument, request.getReferenceTargets());

        activityService.recordActivity(savedDocument, user.getId(), ActivityType.CREATE, "문서를 작성했습니다.");

        return convertToResponse(savedDocument, user);
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, UpdateDocumentRequest request, UserPrincipal user) {
        ApprovalDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!permissionService.canEditDocument(document, user)) {
            throw new UnauthorizedException("문서 수정 권한이 없습니다.");
        }

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new UnauthorizedException("임시저장 상태의 문서만 수정할 수 있습니다.");
        }

        // 템플릿 옵션 검증
        validateTemplateOptions(document.getTemplate(), request.getContent(), request.getAttachments(), request.getApprovalStages());

        document.setContent(request.getContent());
        
        // 첨부파일 업데이트
        if (request.getAttachments() != null) {
            List<AttachmentInfo> attachments = attachmentService.validateAndConvertAttachments(request.getAttachments());
            document.getAttachments().clear();
            document.getAttachments().addAll(attachments);
        }

        // Update related entities using helper methods
        if (request.getFieldValues() != null) {
            document.getFieldValues().clear();
            saveFieldValues(document, request.getFieldValues());
        }
        
        if (request.getApprovalStages() != null) {
            document.getApprovalStages().clear();
            saveApprovalStages(document, request.getApprovalStages());
        }
        
        if (request.getReferenceTargets() != null) {
            document.getReferenceTargets().clear();
            saveReferenceTargets(document, request.getReferenceTargets());
        }

        activityService.recordActivity(document, user.getId(), ActivityType.UPDATE, "문서를 수정했습니다.");

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
            throw new BusinessException("임시저장 상태의 문서만 제출할 수 있습니다.");
        }

        document.setStatus(DocumentStatus.IN_PROGRESS);
        document.setSubmittedAt(LocalDateTime.now());
        document.setCurrentStage(1);

        activityService.recordActivity(document, userId, ActivityType.SUBMIT, "결재를 요청했습니다.");
    }

    @Transactional
    public void deleteDocument(Long id, UserPrincipal user) {
        ApprovalDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!permissionService.canDeleteDocument(document, user)) {
            throw new UnauthorizedException("문서 삭제 권한이 없습니다.");
        }

        activityService.recordActivity(document, user.getId(), ActivityType.DELETE, "문서를 삭제했습니다.");
        documentRepository.delete(document);
    }

    private DocumentSummaryResponse convertToSummaryResponse(ApprovalDocument document, UserPrincipal user) {
        DocumentSummaryResponse response = new DocumentSummaryResponse();
        response.setId(document.getId());
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
        response.setTemplate(responseConverter.convertToTemplateResponse(document.getTemplate()));
        
        // Field values 변환
        response.setFieldValues(document.getFieldValues().stream()
                .map(responseConverter::convertToDocumentFieldValueResponse)
                .toList());
        
        // Approval stages 변환  
        response.setApprovalStages(document.getApprovalStages().stream()
                .map(responseConverter::convertToApprovalStageResponse)
                .toList());
        
        // Reference targets 변환
        response.setReferenceTargets(document.getReferenceTargets().stream()
                .map(responseConverter::convertToApprovalTargetResponse)
                .toList());
        
        // Activities 변환
        response.setActivities(document.getActivities().stream()
                .map(responseConverter::convertToDocumentActivityResponse)
                .toList());
        
        // Comments 변환
        response.setComments(document.getComments().stream()
                .map(responseConverter::convertToDocumentCommentResponse)
                .toList());
        
        // 첨부파일 정보 변환
        response.setAttachments(attachmentService.convertToResponseList(document.getAttachments()));
        
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        response.setSubmittedAt(document.getSubmittedAt());
        response.setApprovedAt(document.getApprovedAt());
        
        return response;
    }

    private void saveFieldValues(ApprovalDocument document, List<DocumentFieldValueRequest> fieldValues) {
        if (fieldValues != null) {
            for (DocumentFieldValueRequest fieldValueRequest : fieldValues) {
                DocumentFieldValue fieldValue = DocumentFieldValue.builder()
                        .fieldName(fieldValueRequest.getFieldName())
                        .fieldValue(fieldValueRequest.getFieldValue())
                        .document(document)
                        .templateField(fieldValueRequest.getTemplateFieldId() != null ? 
                            templateFieldRepository.findById(fieldValueRequest.getTemplateFieldId()).orElse(null) : null)
                        .build();
                document.getFieldValues().add(fieldValue);
            }
        }
    }

    private void saveApprovalStages(ApprovalDocument document, List<ApprovalStageRequest> approvalStages) {
        if (approvalStages != null) {
            for (ApprovalStageRequest stageRequest : approvalStages) {
                DocumentApprovalStage stage = DocumentApprovalStage.builder()
                        .stageOrder(stageRequest.getStageOrder())
                        .stageName(stageRequest.getStageName())
                        .document(document)
                        .build();
                
                saveApprovalTargetsForStage(stage, stageRequest.getApprovalTargets());
                document.getApprovalStages().add(stage);
            }
        }
    }

    private void saveApprovalTargetsForStage(DocumentApprovalStage stage, List<ApprovalTargetRequest> approvalTargets) {
        if (approvalTargets != null) {
            for (ApprovalTargetRequest targetRequest : approvalTargets) {
                DocumentApprovalTarget target = DocumentApprovalTarget.builder()
                        .targetType(targetRequest.getTargetType())
                        .userId(targetRequest.getUserId())
                        .organizationId(targetRequest.getOrganizationId())
                        .managerLevel(targetRequest.getManagerLevel())
                        .isReference(targetRequest.getIsReference())
                        .document(stage.getDocument())
                        .approvalStage(stage)
                        .build();
                stage.getApprovalTargets().add(target);
            }
        }
    }

    private void saveReferenceTargets(ApprovalDocument document, List<ApprovalTargetRequest> referenceTargets) {
        if (referenceTargets != null) {
            for (ApprovalTargetRequest referenceRequest : referenceTargets) {
                DocumentApprovalTarget referenceTarget = DocumentApprovalTarget.builder()
                        .targetType(referenceRequest.getTargetType())
                        .userId(referenceRequest.getUserId())
                        .organizationId(referenceRequest.getOrganizationId())
                        .managerLevel(referenceRequest.getManagerLevel())
                        .isReference(true) // 참조 대상은 항상 true
                        .document(document)
                        .build();
                document.getReferenceTargets().add(referenceTarget);
            }
        }
    }

    /**
     * 템플릿 옵션에 따른 요청 데이터 검증
     */
    private void validateTemplateOptions(DocumentTemplate template, String content,
                                        List<String> attachments, List<ApprovalStageRequest> approvalStages) {
        // useBody 옵션 검증
        if (!template.getUseBody() && StringUtils.hasText(content)) {
            throw new BusinessException("이 템플릿은 본문 입력을 허용하지 않습니다.");
        }

        // useAttachment 옵션 검증
        AttachmentUsageType attachmentType = template.getUseAttachment();
        if (attachmentType == AttachmentUsageType.DISABLED && attachments != null && !attachments.isEmpty()) {
            throw new BusinessException("이 템플릿은 첨부파일을 허용하지 않습니다.");
        } else if (attachmentType == AttachmentUsageType.REQUIRED && (attachments == null || attachments.isEmpty())) {
            throw new BusinessException("이 템플릿은 첨부파일이 필수입니다.");
        }

        // allowTargetChange 옵션 검증
        if (!template.getAllowTargetChange() && approvalStages != null && !approvalStages.isEmpty()) {
            throw new BusinessException("이 템플릿은 승인 대상 변경을 허용하지 않습니다.");
        }
    }
}