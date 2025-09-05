package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.request.ApprovalActionRequest;
import com.hermes.approvalservice.dto.request.CreateDocumentRequest;
import com.hermes.approvalservice.dto.request.UpdateDocumentRequest;
import com.hermes.approvalservice.dto.response.DocumentResponse;
import com.hermes.approvalservice.dto.response.DocumentSummaryResponse;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.service.ApprovalDocumentService;
import com.hermes.approvalservice.service.ApprovalProcessService;
import com.hermes.auth.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/approval/documents")
@RequiredArgsConstructor
@Tag(name = "결재 문서 관리", description = "결재 문서 생성, 조회, 수정 및 승인 처리 API")
public class ApprovalDocumentController {

    private final ApprovalDocumentService documentService;
    private final ApprovalProcessService approvalProcessService;

    @Operation(summary = "문서 목록 조회", description = "현재 사용자가 접근할 수 있는 문서 목록을 필터링하여 페이지네이션으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<Page<DocumentSummaryResponse>> getDocuments(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 상태 필터 (여러 개 선택 가능)") @RequestParam(required = false) List<DocumentStatus> status,
            @Parameter(description = "검색 키워드 (템플릿 제목 또는 작성자 이름)") @RequestParam(required = false) String search,
            @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd)") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd)") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "페이지네이션 정보 (기본 크기: 20)") @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentSummaryResponse> documents = documentService.getDocumentsForUser(
                user, status, search, startDate, endDate, pageable);
        return ResponseEntity.ok(documents);
    }


    @Operation(summary = "문서 상세 조회", description = "지정한 ID의 문서 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "문서 조회 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long id) {
        DocumentResponse document = documentService.getDocumentById(id, user);
        return ResponseEntity.ok(document);
    }

    @Operation(summary = "문서 작성", description = "새로운 결재 문서를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 작성 요청 정보", required = true) @Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse document = documentService.createDocument(request, user);
        return ResponseEntity.ok(document);
    }

    @Operation(summary = "문서 수정", description = "기존 결재 문서를 수정합니다. (임시저장 상태에서만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "문서 수정 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long id,
            @Parameter(description = "문서 수정 요청 정보", required = true) @Valid @RequestBody UpdateDocumentRequest request) {
        DocumentResponse document = documentService.updateDocument(id, request, user);
        return ResponseEntity.ok(document);
    }

    @Operation(summary = "문서 제출", description = "임시저장된 문서를 결재 프로세스에 제출합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 제출 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "문서 제출 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "409", description = "이미 제출된 문서입니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submitDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long id) {
        Long userId = user.getId();
        documentService.submitDocument(id, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "문서 승인", description = "제출된 문서를 승인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 승인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "문서 승인 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "409", description = "승인할 수 없는 문서 상태입니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long id,
            @Parameter(description = "승인 처리 요청 정보") @RequestBody ApprovalActionRequest request) {
        approvalProcessService.approveDocument(id, user, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "문서 반려", description = "제출된 문서를 반려합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 반려 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "문서 반려 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "409", description = "반려할 수 없는 문서 상태입니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long id,
            @Parameter(description = "반려 처리 요청 정보", required = true) @RequestBody ApprovalActionRequest request) {
        approvalProcessService.rejectDocument(id, user, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "문서 삭제", description = "문서를 삭제합니다. 일반 사용자는 본인이 작성한 임시저장 상태의 문서만 삭제할 수 있으며, 관리자는 모든 문서를 삭제할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문서 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "403", description = "문서 삭제 권한이 없습니다"),
            @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "409", description = "삭제할 수 없는 문서 상태입니다"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long id) {
        documentService.deleteDocument(id, user);
        return ResponseEntity.ok().build();
    }
}