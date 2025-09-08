package com.hermes.approvalservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.approvalservice.client.UserServiceClient;
import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.converter.ResponseConverter;
import com.hermes.approvalservice.dto.request.CreateCommentRequest;
import com.hermes.approvalservice.dto.response.DocumentCommentResponse;
import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentComment;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.repository.ApprovalDocumentRepository;
import com.hermes.approvalservice.repository.DocumentCommentRepository;
import com.hermes.approvalservice.service.DocumentPermissionService;
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
@RequestMapping("/api/approval/documents/{documentId}/comments")
@RequiredArgsConstructor
@Tag(name = "문서 댓글 관리", description = "결재 문서의 댓글 조회, 작성 API")
public class DocumentCommentController {

    private final DocumentCommentRepository commentRepository;
    private final ApprovalDocumentRepository documentRepository;
    private final DocumentPermissionService permissionService;
    private final UserServiceClient userServiceClient;
    private final ResponseConverter responseConverter;

    @Operation(summary = "문서 댓글 목록 조회", description = "지정한 문서의 댓글 목록을 시간순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<List<DocumentCommentResponse>> getComments(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long documentId) {
        ApprovalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        
        if (!permissionService.canViewDocument(document, user)) {
            return ResponseEntity.status(403).build();
        }

        List<DocumentCommentResponse> comments = commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(responseConverter::convertToDocumentCommentResponse)
                .toList();

        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "문서 댓글 작성", description = "지정한 문서에 새로운 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<DocumentCommentResponse> createComment(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "문서 ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "댓글 작성 요청 정보", required = true) @Valid @RequestBody CreateCommentRequest request) {
        ApprovalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        
        if (!permissionService.canViewDocument(document, user)) {
            return ResponseEntity.status(403).build();
        }
        Long userId = user.getId();

        DocumentComment comment = DocumentComment.builder()
                .content(request.getContent())
                .authorId(userId)
                .document(document)
                .build();

        DocumentComment savedComment = commentRepository.save(comment);
        DocumentCommentResponse response = responseConverter.convertToDocumentCommentResponse(savedComment);

        return ResponseEntity.ok(response);
    }

}