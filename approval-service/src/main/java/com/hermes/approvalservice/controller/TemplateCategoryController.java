package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.approvalservice.dto.request.CreateCategoryRequest;
import com.hermes.approvalservice.dto.request.UpdateCategoryRequest;
import com.hermes.approvalservice.dto.response.CategoryResponse;
import com.hermes.approvalservice.service.TemplateCategoryService;
import com.hermes.jwt.util.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval/categories")
@RequiredArgsConstructor
public class TemplateCategoryController {

    private final TemplateCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories;
        
        if (AuthUtils.isAdmin()) {
            categories = categoryService.getAllCategories();
        } else {
            categories = categoryService.getCategoriesWithVisibleTemplates();
        }
        
        return ResponseEntity.ok(ApiResponse.success("카테고리 목록을 조회했습니다.", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("카테고리를 조회했습니다.", category));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        if (!AuthUtils.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 카테고리를 생성할 수 있습니다."));
        }
        
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("카테고리를 생성했습니다.", category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateCategoryRequest request) {
        if (!AuthUtils.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 카테고리를 수정할 수 있습니다."));
        }
        
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("카테고리를 수정했습니다.", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        if (!AuthUtils.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 카테고리를 삭제할 수 있습니다."));
        }
        
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("카테고리를 삭제했습니다."));
    }
}