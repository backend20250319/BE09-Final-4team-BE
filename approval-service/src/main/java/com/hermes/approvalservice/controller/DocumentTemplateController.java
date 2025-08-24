package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.approvalservice.dto.request.CreateTemplateRequest;
import com.hermes.approvalservice.dto.request.UpdateTemplateRequest;
import com.hermes.approvalservice.dto.response.TemplateResponse;
import com.hermes.approvalservice.dto.response.TemplatesByCategoryResponse;
import com.hermes.approvalservice.service.DocumentTemplateService;
import com.hermes.jwt.context.AuthContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval/templates")
@RequiredArgsConstructor
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplates(
            @RequestParam(required = false) Long categoryId) {
        List<TemplateResponse> templates;
        
        if (categoryId != null) {
            templates = templateService.getTemplatesByCategory(categoryId, AuthContext.isCurrentUserAdmin());
        } else {
            templates = templateService.getAllTemplates(AuthContext.isCurrentUserAdmin());
        }
        
        return ResponseEntity.ok(ApiResponse.success("템플릿 목록을 조회했습니다.", templates));
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<List<TemplatesByCategoryResponse>>> getTemplatesByCategory() {
        List<TemplatesByCategoryResponse> templates = templateService.getTemplatesByCategory(AuthContext.isCurrentUserAdmin());
        return ResponseEntity.ok(ApiResponse.success("카테고리별 템플릿 목록을 조회했습니다.", templates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplateById(@PathVariable Long id) {
        TemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 조회했습니다.", template));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿을 생성할 수 있습니다."));
        }
        
        TemplateResponse template = templateService.createTemplate(request);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 생성했습니다.", template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTemplateRequest request) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿을 수정할 수 있습니다."));
        }
        
        TemplateResponse template = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 수정했습니다.", template));
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<ApiResponse<Void>> updateTemplateVisibility(
            @PathVariable Long id,
            @RequestParam boolean isHidden) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿 숨김 처리를 할 수 있습니다."));
        }
        
        templateService.updateTemplateVisibility(id, isHidden);
        String message = isHidden ? "템플릿을 숨김 처리했습니다." : "템플릿 숨김을 해제했습니다.";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿을 삭제할 수 있습니다."));
        }
        
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 삭제했습니다."));
    }
}