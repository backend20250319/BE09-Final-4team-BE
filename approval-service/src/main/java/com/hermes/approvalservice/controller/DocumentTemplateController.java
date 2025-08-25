package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.approvalservice.dto.request.CreateTemplateRequest;
import com.hermes.approvalservice.dto.request.UpdateTemplateRequest;
import com.hermes.approvalservice.dto.response.TemplateResponse;
import com.hermes.approvalservice.dto.response.TemplatesByCategoryResponse;
import com.hermes.approvalservice.service.DocumentTemplateService;
import com.hermes.auth.context.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval/templates")
@RequiredArgsConstructor
@Tag(name = "Document Templates", description = "결재 문서 템플릿 관리 API")
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;

    @GetMapping
    @Operation(summary = "템플릿 목록 조회", description = "카테고리별 또는 전체 템플릿 목록을 조회합니다")
    @ApiResponses({
            @SwaggerApiResponse(responseCode = "200", description = "템플릿 목록 조회 성공"),
            @SwaggerApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplates(
            @Parameter(description = "카테고리 ID (선택사항)") @RequestParam(required = false) Long categoryId) {
        List<TemplateResponse> templates;
        
        if (categoryId != null) {
            templates = templateService.getTemplatesByCategory(categoryId, AuthContext.isCurrentUserAdmin());
        } else {
            templates = templateService.getAllTemplates(AuthContext.isCurrentUserAdmin());
        }
        
        return ResponseEntity.ok(ApiResponse.success("템플릿 목록을 조회했습니다.", templates));
    }

    @GetMapping("/by-category")
    @Operation(summary = "카테고리별 템플릿 조회", description = "카테고리별로 그룹화된 템플릿 목록을 조회합니다")
    @ApiResponses({
            @SwaggerApiResponse(responseCode = "200", description = "카테고리별 템플릿 목록 조회 성공")
    })
    public ResponseEntity<ApiResponse<List<TemplatesByCategoryResponse>>> getTemplatesByCategory() {
        List<TemplatesByCategoryResponse> templates = templateService.getTemplatesByCategory(AuthContext.isCurrentUserAdmin());
        return ResponseEntity.ok(ApiResponse.success("카테고리별 템플릿 목록을 조회했습니다.", templates));
    }

    @GetMapping("/{id}")
    @Operation(summary = "템플릿 상세 조회", description = "ID로 특정 템플릿의 상세 정보를 조회합니다")
    @ApiResponses({
            @SwaggerApiResponse(responseCode = "200", description = "템플릿 조회 성공"),
            @SwaggerApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplateById(
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id) {
        TemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 조회했습니다.", template));
    }

    @PostMapping
    @Operation(summary = "템플릿 생성", description = "새로운 결재 템플릿을 생성합니다 (관리자 전용)")
    @ApiResponses({
            @SwaggerApiResponse(responseCode = "200", description = "템플릿 생성 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @SwaggerApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Parameter(description = "템플릿 생성 요청", required = true) @Valid @RequestBody CreateTemplateRequest request) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿을 생성할 수 있습니다."));
        }
        
        TemplateResponse template = templateService.createTemplate(request);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 생성했습니다.", template));
    }

    @PutMapping("/{id}")
    @Operation(summary = "템플릿 수정", description = "기존 템플릿을 수정합니다 (관리자 전용)")
    @ApiResponses({
            @SwaggerApiResponse(responseCode = "200", description = "템플릿 수정 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @SwaggerApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @SwaggerApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id,
            @Parameter(description = "템플릿 수정 요청", required = true) @Valid @RequestBody UpdateTemplateRequest request) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿을 수정할 수 있습니다."));
        }
        
        TemplateResponse template = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 수정했습니다.", template));
    }

    @PutMapping("/{id}/visibility")
    @Operation(summary = "템플릿 공개/숨김 설정", description = "템플릿의 공개 여부를 설정합니다 (관리자 전용)")
    @ApiResponses({
            @SwaggerApiResponse(responseCode = "200", description = "템플릿 공개 설정 변경 성공"),
            @SwaggerApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @SwaggerApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> updateTemplateVisibility(
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id,
            @Parameter(description = "숨김 여부", required = true) @RequestParam boolean isHidden) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿 숨김 처리를 할 수 있습니다."));
        }
        
        templateService.updateTemplateVisibility(id, isHidden);
        String message = isHidden ? "템플릿을 숨김 처리했습니다." : "템플릿 숨김을 해제했습니다.";
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "템플릿 삭제", description = "템플릿을 삭제합니다 (관리자 전용)")
    @ApiResponses({
            @SwaggerApiResponse(responseCode = "200", description = "템플릿 삭제 성공"),
            @SwaggerApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @SwaggerApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id) {
        if (!AuthContext.isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("관리자만 템플릿을 삭제할 수 있습니다."));
        }
        
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("템플릿을 삭제했습니다."));
    }
}