package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.approvalservice.dto.request.ApprovalActionRequest;
import com.hermes.approvalservice.dto.request.CreateDocumentRequest;
import com.hermes.approvalservice.dto.request.UpdateDocumentRequest;
import com.hermes.approvalservice.dto.response.DocumentResponse;
import com.hermes.approvalservice.dto.response.DocumentSummaryResponse;
import com.hermes.approvalservice.service.ApprovalDocumentService;
import com.hermes.approvalservice.service.ApprovalProcessService;
import com.hermes.jwt.util.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approval/documents")
@RequiredArgsConstructor
public class ApprovalDocumentController {

    private final ApprovalDocumentService documentService;
    private final ApprovalProcessService approvalProcessService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DocumentSummaryResponse>>> getDocuments(
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = AuthUtils.getCurrentUserId();
        Page<DocumentSummaryResponse> documents = documentService.getDocumentsForUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("문서 목록을 조회했습니다.", documents));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<DocumentSummaryResponse>>> getPendingApprovals(
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = AuthUtils.getCurrentUserId();
        Page<DocumentSummaryResponse> documents = documentService.getPendingApprovals(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("승인 대기 문서 목록을 조회했습니다.", documents));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(@PathVariable Long id) {
        Long userId = AuthUtils.getCurrentUserId();
        DocumentResponse document = documentService.getDocumentById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("문서를 조회했습니다.", document));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        Long userId = AuthUtils.getCurrentUserId();
        DocumentResponse document = documentService.createDocument(request, userId);
        return ResponseEntity.ok(ApiResponse.success("문서를 작성했습니다.", document));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDocumentRequest request) {
        Long userId = AuthUtils.getCurrentUserId();
        DocumentResponse document = documentService.updateDocument(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("문서를 수정했습니다.", document));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitDocument(@PathVariable Long id) {
        Long userId = AuthUtils.getCurrentUserId();
        documentService.submitDocument(id, userId);
        return ResponseEntity.ok(ApiResponse.success("문서를 제출했습니다."));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveDocument(
            @PathVariable Long id,
            @RequestBody ApprovalActionRequest request) {
        Long userId = AuthUtils.getCurrentUserId();
        approvalProcessService.approveDocument(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("문서를 승인했습니다."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectDocument(
            @PathVariable Long id,
            @RequestBody ApprovalActionRequest request) {
        Long userId = AuthUtils.getCurrentUserId();
        approvalProcessService.rejectDocument(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("문서를 반려했습니다."));
    }
}