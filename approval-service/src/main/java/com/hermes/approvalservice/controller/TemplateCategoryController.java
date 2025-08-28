package com.hermes.approvalservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.approvalservice.dto.request.CreateCategoryRequest;
import com.hermes.approvalservice.dto.request.UpdateCategoryRequest;
import com.hermes.approvalservice.dto.response.CategoryResponse;
import com.hermes.approvalservice.service.TemplateCategoryService;
import com.hermes.auth.principal.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<ApiResult<List<CategoryResponse>>> getCategories(
            @AuthenticationPrincipal UserPrincipal user) {
        List<CategoryResponse> categories;
        
        if (user.isAdmin()) {
            categories = categoryService.getAllCategories();
        } else {
            categories = categoryService.getCategoriesWithVisibleTemplates();
        }
        
        return ResponseEntity.ok(ApiResult.success("카테고리 목록을 조회했습니다.", categories));
    }

    @Operation(summary = "카테고리 상세 조회", description = "지정한 ID의 카테고리 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<CategoryResponse>> getCategoryById(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResult.success("카테고리를 조회했습니다.", category));
    }

    @Operation(summary = "카테고리 생성", description = "새로운 템플릿 카테고리를 생성합니다. (관리자만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "관리자만 카테고리를 생성할 수 있습니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<ApiResult<CategoryResponse>> createCategory(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "카테고리 생성 요청 정보", required = true) @Valid @RequestBody CreateCategoryRequest request) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResult.rejected("관리자만 카테고리를 생성할 수 있습니다."));
        }
        
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResult.success("카테고리를 생성했습니다.", category));
    }

    @Operation(summary = "카테고리 수정", description = "기존 템플릿 카테고리를 수정합니다. (관리자만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "관리자만 카테고리를 수정할 수 있습니다"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<CategoryResponse>> updateCategory(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id, 
            @Parameter(description = "카테고리 수정 요청 정보", required = true) @Valid @RequestBody UpdateCategoryRequest request) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResult.rejected("관리자만 카테고리를 수정할 수 있습니다."));
        }
        
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResult.success("카테고리를 수정했습니다.", category));
    }

    @Operation(summary = "카테고리 삭제", description = "기존 템플릿 카테고리를 삭제합니다. (관리자만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "관리자만 카테고리를 삭제할 수 있습니다"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "409", description = "사용중인 카테고리는 삭제할 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deleteCategory(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body(ApiResult.rejected("관리자만 카테고리를 삭제할 수 있습니다."));
        }
        
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResult.success("카테고리를 삭제했습니다."));
    }
}