package com.hermes.approvalservice.service;

import com.hermes.approvalservice.dto.request.CreateCategoryRequest;
import com.hermes.approvalservice.dto.request.UpdateCategoryRequest;
import com.hermes.approvalservice.dto.response.CategoryResponse;
import com.hermes.approvalservice.entity.TemplateCategory;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.repository.TemplateCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .description(request.getDescription())
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
        category.setDescription(request.getDescription());
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

    private CategoryResponse convertToResponse(TemplateCategory category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSortOrder(category.getSortOrder());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}