package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.request.CreateCategoryRequest;
import com.hermes.approvalservice.dto.request.UpdateCategoryRequest;
import com.hermes.approvalservice.dto.request.BulkCategoryRequest;
import com.hermes.approvalservice.dto.response.CategoryResponse;
import com.hermes.approvalservice.dto.response.BulkCategoryResponse;
import com.hermes.approvalservice.service.TemplateCategoryService;
import com.hermes.auth.principal.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval/categories")
@RequiredArgsConstructor
@Tag(name = "템플릿 카테고리 관리", description = "결재 문서 템플릿 카테고리 생성, 조회, 수정, 삭제 API")
public class TemplateCategoryController {

    private final TemplateCategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "사용자 권한에 따라 전체 카테고리 또는 공개 카테고리 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @AuthenticationPrincipal UserPrincipal user) {
        List<CategoryResponse> categories;
        
        if (user.isAdmin()) {
            categories = categoryService.getAllCategories();
        } else {
            categories = categoryService.getCategoriesWithVisibleTemplates();
        }
        
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "카테고리 상세 조회", description = "지정한 ID의 카테고리 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "카테고리 생성", description = "새로운 템플릿 카테고리를 생성합니다. (관리자만 가능)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "카테고리 생성 요청 정보", required = true) @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "카테고리 수정", description = "기존 템플릿 카테고리를 수정합니다. (관리자만 가능)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id, 
            @Parameter(description = "카테고리 수정 요청 정보", required = true) @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "카테고리 삭제", description = "기존 템플릿 카테고리를 삭제합니다. (관리자만 가능)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 벌크 작업", description = "여러 카테고리를 한 번에 생성, 수정, 삭제합니다. (관리자만 가능)")
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkCategoryResponse> bulkProcessCategories(
            @Parameter(description = "벌크 카테고리 작업 요청 정보", required = true) @Valid @RequestBody BulkCategoryRequest request) {
        BulkCategoryResponse response = categoryService.bulkProcessCategories(request);
        return ResponseEntity.ok(response);
    }
}