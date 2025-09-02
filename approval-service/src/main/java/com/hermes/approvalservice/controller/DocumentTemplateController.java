package com.hermes.approvalservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.approvalservice.dto.request.CreateTemplateRequest;
import com.hermes.approvalservice.dto.request.UpdateTemplateRequest;
import com.hermes.approvalservice.dto.response.TemplateResponse;
import com.hermes.approvalservice.dto.response.TemplateSummaryResponse;
import com.hermes.approvalservice.dto.response.TemplatesByCategoryResponse;
import com.hermes.approvalservice.service.DocumentTemplateService;
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
@RequestMapping("/api/approval/templates")
@RequiredArgsConstructor
@Tag(name = "Document Templates", description = "결재 문서 템플릿 관리 API")
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;

    @GetMapping
    @Operation(summary = "템플릿 목록 조회", description = "카테고리별 또는 전체 템플릿 목록을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "템플릿 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<ApiResult<List<TemplateSummaryResponse>>> getTemplates(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "카테고리 ID (선택사항)") @RequestParam(required = false) Long categoryId) {
        List<TemplateSummaryResponse> templates;
        boolean isAdmin = user.isAdmin();
        
        if (categoryId != null) {
            templates = templateService.getTemplatesByCategory(categoryId, isAdmin);
        } else {
            templates = templateService.getAllTemplates(isAdmin);
        }
        
        return ResponseEntity.ok(ApiResult.success("템플릿 목록을 조회했습니다.", templates));
    }

    @GetMapping("/by-category")
    @Operation(summary = "카테고리별 템플릿 조회", description = "카테고리별로 그룹화된 템플릿 목록을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리별 템플릿 목록 조회 성공")
    })
    public ResponseEntity<ApiResult<List<TemplatesByCategoryResponse>>> getTemplatesByCategory(
            @AuthenticationPrincipal UserPrincipal user) {
        List<TemplatesByCategoryResponse> templates = templateService.getTemplatesByCategory(user.isAdmin());
        return ResponseEntity.ok(ApiResult.success("카테고리별 템플릿 목록을 조회했습니다.", templates));
    }

    @GetMapping("/{id}")
    @Operation(summary = "템플릿 상세 조회", description = "ID로 특정 템플릿의 상세 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "템플릿 조회 성공"),
            @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<TemplateResponse>> getTemplateById(
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id) {
        TemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResult.success("템플릿을 조회했습니다.", template));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "템플릿 생성", description = "새로운 결재 템플릿을 생성합니다 (관리자 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "템플릿 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public ResponseEntity<ApiResult<TemplateResponse>> createTemplate(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "템플릿 생성 요청", required = true) @Valid @RequestBody CreateTemplateRequest request) {
        TemplateResponse template = templateService.createTemplate(request);
        return ResponseEntity.ok(ApiResult.success("템플릿을 생성했습니다.", template));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "템플릿 수정", description = "기존 템플릿을 수정합니다 (관리자 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "템플릿 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<TemplateResponse>> updateTemplate(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id,
            @Parameter(description = "템플릿 수정 요청", required = true) @Valid @RequestBody UpdateTemplateRequest request) {
        TemplateResponse template = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResult.success("템플릿을 수정했습니다.", template));
    }

    @PutMapping("/{id}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "템플릿 공개/숨김 설정", description = "템플릿의 공개 여부를 설정합니다 (관리자 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "템플릿 공개 설정 변경 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<Void>> updateTemplateVisibility(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id,
            @Parameter(description = "숨김 여부", required = true) @RequestParam boolean isHidden) {
        templateService.updateTemplateVisibility(id, isHidden);
        String message = isHidden ? "템플릿을 숨김 처리했습니다." : "템플릿 숨김을 해제했습니다.";
        return ResponseEntity.ok(ApiResult.success(message));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "템플릿 삭제", description = "템플릿을 삭제합니다 (관리자 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "템플릿 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<Void>> deleteTemplate(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "템플릿 ID", required = true) @PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResult.success("템플릿을 삭제했습니다."));
    }
}