package com.hermes.approvalservice.service;

import com.hermes.attachment.entity.AttachmentInfo;
import com.hermes.attachment.service.AttachmentClientService;
import com.hermes.approvalservice.dto.request.CreateDocumentRequest;
import com.hermes.approvalservice.dto.request.UpdateDocumentRequest;
import com.hermes.approvalservice.dto.response.DocumentResponse;
import com.hermes.approvalservice.dto.response.DocumentSummaryResponse;
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


    public Page<DocumentSummaryResponse> getDocumentsForUser(Long userId, UserPrincipal user, 
                                                            List<DocumentStatus> statuses, String search, 
                                                            LocalDate startDate, LocalDate endDate, 
                                                            Pageable pageable) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        return documentRepository.findDocumentsForUserWithFilters(userId, statuses, search, 
                                                                 startDateTime, endDateTime, pageable)
                .map(document -> convertToSummaryResponse(document, userId, user));
    }


    public DocumentResponse getDocumentById(Long id, Long userId, UserPrincipal user) {
        ApprovalDocument document = documentRepository.findByIdWithDetails(id);
        if (document == null) {
            throw new NotFoundException("문서를 찾을 수 없습니다.");
        }

        if (!permissionService.canViewDocument(document, userId, user)) {
            throw new UnauthorizedException("문서 조회 권한이 없습니다.");
        }

        return convertToResponse(document);
    }

    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request, Long authorId) {
        DocumentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다."));

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

        return convertToResponse(savedDocument);
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, UpdateDocumentRequest request, Long userId, UserPrincipal user) {
        ApprovalDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!permissionService.canEditDocument(document, userId, user)) {
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

        return convertToResponse(document);
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

    private DocumentSummaryResponse convertToSummaryResponse(ApprovalDocument document, Long userId, UserPrincipal user) {
        DocumentSummaryResponse response = new DocumentSummaryResponse();
        response.setId(document.getId());
        response.setTitle(document.getTitle());
        response.setContent(document.getContent());
        response.setStatus(document.getStatus());
        response.setAuthorId(document.getAuthorId());
        response.setTemplateTitle(document.getTemplate().getTitle());
        response.setCurrentStage(document.getCurrentStage());
        response.setTotalStages(document.getApprovalStages().size());
        
        // Set user role if user information is available
        if (user != null) {
            response.setUserRole(permissionService.getUserRole(document, userId, user));
        }
        
        response.setCreatedAt(document.getCreatedAt());
        response.setSubmittedAt(document.getSubmittedAt());
        response.setApprovedAt(document.getApprovedAt());
        return response;
    }

    private DocumentResponse convertToResponse(ApprovalDocument document) {
        // Full conversion implementation
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setTitle(document.getTitle());
        response.setContent(document.getContent());
        response.setStatus(document.getStatus());
        response.setAuthorId(document.getAuthorId());
        response.setCurrentStage(document.getCurrentStage());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        response.setSubmittedAt(document.getSubmittedAt());
        response.setApprovedAt(document.getApprovedAt());
        
        // 첨부파일 정보 변환
        response.setAttachments(attachmentService.convertToResponseList(document.getAttachments()));
        
        // Add template, field values, stages, etc.
        return response;
    }
}