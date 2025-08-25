package com.hermes.approvalservice.service;

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

    public List<TemplateResponse> getAllTemplates(boolean isAdmin) {
        List<DocumentTemplate> templates = isAdmin 
            ? templateRepository.findAll()
            : templateRepository.findByIsHiddenFalse();
        
        return templates.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<TemplateResponse> getTemplatesByCategory(Long categoryId, boolean isAdmin) {
        List<DocumentTemplate> templates = isAdmin
            ? templateRepository.findByCategoryId(categoryId)
            : templateRepository.findByCategoryIdAndIsHiddenFalse(categoryId);
        
        return templates.stream()
                .map(this::convertToResponse)
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
                            .map(this::convertToResponse)
                            .toList());
                    return response;
                })
                .toList();
    }

    public TemplateResponse getTemplateById(Long id) {
        DocumentTemplate template = templateRepository.findByIdWithDetails(id);
        if (template == null) {
            throw new NotFoundException("템플릿을 찾을 수 없습니다.");
        }
        return convertToResponse(template);
    }

    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        TemplateCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
        }

        DocumentTemplate template = DocumentTemplate.builder()
                .title(request.getTitle())
                .icon(request.getIcon())
                .description(request.getDescription())
                .bodyTemplate(request.getBodyTemplate())
                .useBody(request.getUseBody())
                .useAttachment(request.getUseAttachment())
                .allowApprovalChange(request.getAllowApprovalChange())
                .allowReferenceChange(request.getAllowReferenceChange())
                .referenceFiles(request.getReferenceFiles())
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

        template.setTitle(request.getTitle());
        template.setIcon(request.getIcon());
        template.setDescription(request.getDescription());
        template.setBodyTemplate(request.getBodyTemplate());
        template.setUseBody(request.getUseBody());
        template.setUseAttachment(request.getUseAttachment());
        template.setAllowApprovalChange(request.getAllowApprovalChange());
        template.setAllowReferenceChange(request.getAllowReferenceChange());
        template.setReferenceFiles(request.getReferenceFiles());
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
        response.setAllowApprovalChange(template.getAllowApprovalChange());
        response.setAllowReferenceChange(template.getAllowReferenceChange());
        response.setIsHidden(template.getIsHidden());
        response.setReferenceFiles(template.getReferenceFiles());
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

        // Convert fields, stages, and targets to responses
        // (Implementation details omitted for brevity)

        return response;
    }
}