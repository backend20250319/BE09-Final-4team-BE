package com.hermes.approvalservice.service;

import com.hermes.attachment.entity.AttachmentInfo;
import com.hermes.attachment.service.AttachmentClientService;
import com.hermes.approvalservice.dto.request.*;
import com.hermes.approvalservice.dto.response.*;
import com.hermes.approvalservice.entity.*;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentTemplateService {

    private final DocumentTemplateRepository templateRepository;
    private final TemplateCategoryRepository categoryRepository;
    private final TemplateFieldRepository fieldRepository;
    private final TemplateApprovalStageRepository stageRepository;
    private final TemplateApprovalTargetRepository targetRepository;
    private final AttachmentClientService attachmentService;

    public List<TemplateSummaryResponse> getAllTemplates(boolean isAdmin) {
        List<DocumentTemplate> templates = isAdmin 
            ? templateRepository.findAll()
            : templateRepository.findByIsHiddenFalse();
        
        return templates.stream()
                .map(this::convertToSummaryResponse)
                .toList();
    }

    public List<TemplateSummaryResponse> getTemplatesByCategory(Long categoryId, boolean isAdmin) {
        List<DocumentTemplate> templates = isAdmin
            ? templateRepository.findByCategoryId(categoryId)
            : templateRepository.findByCategoryIdAndIsHiddenFalse(categoryId);
        
        return templates.stream()
                .map(this::convertToSummaryResponse)
                .toList();
    }

    public List<TemplatesByCategoryResponse> getTemplatesByCategory(boolean isAdmin) {
        List<DocumentTemplate> templates = isAdmin
            ? templateRepository.findAll()
            : templateRepository.findVisibleTemplatesWithCategory();

        Map<TemplateCategory, List<DocumentTemplate>> groupedTemplates = templates.stream()
                .collect(Collectors.groupingBy(DocumentTemplate::getCategory));

        return groupedTemplates.entrySet().stream()
                .map(entry -> {
                    TemplatesByCategoryResponse response = new TemplatesByCategoryResponse();
                    response.setCategoryId(entry.getKey().getId());
                    response.setCategoryName(entry.getKey().getName());
                    response.setTemplates(entry.getValue().stream()
                            .map(this::convertToSummaryResponse)
                            .toList());
                    return response;
                })
                .toList();
    }

    public TemplateResponse getTemplateById(Long id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다."));
        return convertToResponse(template);
    }

    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        TemplateCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
        }

        // 참조 파일 검증 및 변환
        List<AttachmentInfo> referenceFiles = attachmentService.validateAndConvertAttachments(request.getReferenceFiles());

        DocumentTemplate template = DocumentTemplate.builder()
                .title(request.getTitle())
                .icon(request.getIcon())
                .description(request.getDescription())
                .bodyTemplate(request.getBodyTemplate())
                .useBody(request.getUseBody())
                .useAttachment(request.getUseAttachment())
                .allowTargetChange(request.getAllowTargetChange())
                .referenceFiles(referenceFiles)
                .category(category)
                .build();

        DocumentTemplate savedTemplate = templateRepository.save(template);

        // Save fields
        if (request.getFields() != null) {
            saveTemplateFields(savedTemplate, request.getFields());
        }

        // Save approval stages
        if (request.getApprovalStages() != null) {
            saveApprovalStages(savedTemplate, request.getApprovalStages());
        }

        // Save reference targets
        if (request.getReferenceTargets() != null) {
            saveReferenceTargets(savedTemplate, request.getReferenceTargets());
        }

        return getTemplateById(savedTemplate.getId());
    }

    @Transactional
    public TemplateResponse updateTemplate(Long id, UpdateTemplateRequest request) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다."));

        TemplateCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
        }

        // 참조 파일 업데이트
        if (request.getReferenceFiles() != null) {
            List<AttachmentInfo> referenceFiles = attachmentService.validateAndConvertAttachments(request.getReferenceFiles());
            template.getReferenceFiles().clear();
            template.getReferenceFiles().addAll(referenceFiles);
        }

        template.setTitle(request.getTitle());
        template.setIcon(request.getIcon());
        template.setDescription(request.getDescription());
        template.setBodyTemplate(request.getBodyTemplate());
        template.setUseBody(request.getUseBody());
        template.setUseAttachment(request.getUseAttachment());
        template.setAllowTargetChange(request.getAllowTargetChange());
        template.setCategory(category);

        // Clear existing fields, stages, and targets
        fieldRepository.deleteByTemplateId(id);
        stageRepository.deleteByTemplateId(id);
        targetRepository.deleteByTemplateId(id);

        // Save new fields
        if (request.getFields() != null) {
            saveTemplateFields(template, request.getFields());
        }

        // Save new approval stages
        if (request.getApprovalStages() != null) {
            saveApprovalStages(template, request.getApprovalStages());
        }

        // Save new reference targets
        if (request.getReferenceTargets() != null) {
            saveReferenceTargets(template, request.getReferenceTargets());
        }

        return getTemplateById(template.getId());
    }

    @Transactional
    public void updateTemplateVisibility(Long id, boolean isHidden) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("템플릿을 찾을 수 없습니다."));
        
        template.setIsHidden(isHidden);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new NotFoundException("템플릿을 찾을 수 없습니다.");
        }
        templateRepository.deleteById(id);
    }

    private void saveTemplateFields(DocumentTemplate template, List<TemplateFieldRequest> fieldRequests) {
        List<TemplateField> fields = fieldRequests.stream()
                .map(request -> TemplateField.builder()
                        .name(request.getName())
                        .fieldType(request.getFieldType())
                        .required(request.getRequired())
                        .fieldOrder(request.getFieldOrder())
                        .options(request.getOptions())
                        .template(template)
                        .build())
                .toList();
        
        fieldRepository.saveAll(fields);
    }

    private void saveApprovalStages(DocumentTemplate template, List<ApprovalStageRequest> stageRequests) {
        for (ApprovalStageRequest stageRequest : stageRequests) {
            TemplateApprovalStage stage = TemplateApprovalStage.builder()
                    .stageOrder(stageRequest.getStageOrder())
                    .stageName(stageRequest.getStageName())
                    .template(template)
                    .build();
            
            TemplateApprovalStage savedStage = stageRepository.save(stage);

            if (stageRequest.getApprovalTargets() != null) {
                List<TemplateApprovalTarget> targets = stageRequest.getApprovalTargets().stream()
                        .map(targetRequest -> TemplateApprovalTarget.builder()
                                .targetType(targetRequest.getTargetType())
                                .userId(targetRequest.getUserId())
                                .organizationId(targetRequest.getOrganizationId())
                                .managerLevel(targetRequest.getManagerLevel())
                                .isReference(targetRequest.getIsReference())
                                .template(template)
                                .approvalStage(savedStage)
                                .build())
                        .toList();
                
                targetRepository.saveAll(targets);
            }
        }
    }

    private void saveReferenceTargets(DocumentTemplate template, List<ApprovalTargetRequest> targetRequests) {
        List<TemplateApprovalTarget> targets = targetRequests.stream()
                .map(request -> TemplateApprovalTarget.builder()
                        .targetType(request.getTargetType())
                        .userId(request.getUserId())
                        .organizationId(request.getOrganizationId())
                        .managerLevel(request.getManagerLevel())
                        .isReference(true)
                        .template(template)
                        .build())
                .toList();
        
        targetRepository.saveAll(targets);
    }

    private TemplateResponse convertToResponse(DocumentTemplate template) {
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
        // 참조 파일 정보 변환
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

        // Convert fields to responses
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

        // Convert approval stages to responses
        response.setApprovalStages(template.getApprovalStages().stream()
                .map(stage -> {
                    ApprovalStageResponse stageResponse = new ApprovalStageResponse();
                    stageResponse.setId(stage.getId());
                    stageResponse.setStageOrder(stage.getStageOrder());
                    stageResponse.setStageName(stage.getStageName());
                    stageResponse.setIsCompleted(false); // 템플릿에서는 완료 상태 없음
                    stageResponse.setCompletedAt(null);
                    
                    // Convert stage's approval targets
                    stageResponse.setApprovalTargets(stage.getApprovalTargets().stream()
                            .map(target -> {
                                ApprovalTargetResponse targetResponse = new ApprovalTargetResponse();
                                targetResponse.setId(target.getId());
                                targetResponse.setTargetType(target.getTargetType());
                                targetResponse.setUserId(target.getUserId());
                                targetResponse.setOrganizationId(target.getOrganizationId());
                                targetResponse.setManagerLevel(target.getManagerLevel());
                                targetResponse.setIsReference(target.getIsReference());
                                targetResponse.setIsApproved(false); // 템플릿에서는 승인 상태 없음
                                targetResponse.setApprovedBy(null);
                                targetResponse.setApprovedAt(null);
                                return targetResponse;
                            })
                            .toList());
                    
                    return stageResponse;
                })
                .toList());

        // Convert reference targets to responses
        response.setReferenceTargets(template.getReferenceTargets().stream()
                .map(target -> {
                    ApprovalTargetResponse targetResponse = new ApprovalTargetResponse();
                    targetResponse.setId(target.getId());
                    targetResponse.setTargetType(target.getTargetType());
                    targetResponse.setUserId(target.getUserId());
                    targetResponse.setOrganizationId(target.getOrganizationId());
                    targetResponse.setManagerLevel(target.getManagerLevel());
                    targetResponse.setIsReference(target.getIsReference());
                    targetResponse.setIsApproved(false); // 템플릿에서는 승인 상태 없음
                    targetResponse.setApprovedBy(null);
                    targetResponse.setApprovedAt(null);
                    return targetResponse;
                })
                .toList());

        return response;
    }

    private TemplateSummaryResponse convertToSummaryResponse(DocumentTemplate template) {
        TemplateSummaryResponse response = new TemplateSummaryResponse();
        response.setId(template.getId());
        response.setTitle(template.getTitle());
        response.setIcon(template.getIcon());
        response.setDescription(template.getDescription());
        response.setUseBody(template.getUseBody());
        response.setUseAttachment(template.getUseAttachment());
        response.setAllowTargetChange(template.getAllowTargetChange());
        response.setIsHidden(template.getIsHidden());
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

        return response;
    }
}