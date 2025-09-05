package com.hermes.approvalservice.service;

import com.hermes.approvalservice.dto.request.CreateCategoryRequest;
import com.hermes.approvalservice.dto.request.UpdateCategoryRequest;
import com.hermes.approvalservice.dto.request.BulkCategoryRequest;
import com.hermes.approvalservice.dto.request.BulkCategoryOperation;
import com.hermes.approvalservice.dto.response.CategoryResponse;
import com.hermes.approvalservice.dto.response.BulkCategoryResponse;
import com.hermes.approvalservice.enums.CategoryOperationType;
import com.hermes.approvalservice.entity.TemplateCategory;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.repository.TemplateCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateCategoryService {

    private final TemplateCategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<CategoryResponse> getCategoriesWithVisibleTemplates() {
        return categoryRepository.findCategoriesWithVisibleTemplates()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        TemplateCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
        return convertToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        TemplateCategory category = TemplateCategory.builder()
                .name(request.getName())
                .sortOrder(request.getSortOrder())
                .build();

        TemplateCategory savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        TemplateCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));

        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());

        return convertToResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("카테고리를 찾을 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }

    @Transactional
    public BulkCategoryResponse bulkProcessCategories(BulkCategoryRequest request) {
        BulkCategoryResponse response = new BulkCategoryResponse();
        List<BulkCategoryResponse.CategoryOperationResult> results = new ArrayList<>();
        
        int totalOperations = request.getOperations().size();
        int successfulOperations = 0;
        int failedOperations = 0;
        
        for (BulkCategoryOperation operation : request.getOperations()) {
            BulkCategoryResponse.CategoryOperationResult result = new BulkCategoryResponse.CategoryOperationResult();
            result.setOperationType(operation.getType().name());
            result.setCategoryId(operation.getId());
            
            try {
                if (operation.getType() == CategoryOperationType.CREATE) {
                    CategoryResponse categoryResponse = createCategory(operation.getCreateRequest());
                    result.setCategory(categoryResponse);
                    result.setSuccess(true);
                    successfulOperations++;
                } else if (operation.getType() == CategoryOperationType.UPDATE) {
                    CategoryResponse categoryResponse = updateCategory(operation.getId(), operation.getUpdateRequest());
                    result.setCategory(categoryResponse);
                    result.setSuccess(true);
                    successfulOperations++;
                } else if (operation.getType() == CategoryOperationType.DELETE) {
                    deleteCategory(operation.getId());
                    result.setSuccess(true);
                    successfulOperations++;
                }
            } catch (Exception e) {
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
                failedOperations++;
            }
            
            results.add(result);
        }
        
        response.setTotalOperations(totalOperations);
        response.setSuccessfulOperations(successfulOperations);
        response.setFailedOperations(failedOperations);
        response.setResults(results);
        
        return response;
    }

    private CategoryResponse convertToResponse(TemplateCategory category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSortOrder(category.getSortOrder());
        return response;
    }
}