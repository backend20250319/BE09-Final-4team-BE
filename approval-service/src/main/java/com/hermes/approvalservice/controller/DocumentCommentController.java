package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.approvalservice.dto.request.CreateCommentRequest;
import com.hermes.approvalservice.dto.response.DocumentCommentResponse;
import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentComment;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.repository.ApprovalDocumentRepository;
import com.hermes.approvalservice.repository.DocumentCommentRepository;
import com.hermes.approvalservice.service.DocumentPermissionService;
import com.hermes.auth.context.AuthContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval/documents/{documentId}/comments")
@RequiredArgsConstructor
public class DocumentCommentController {

    private final DocumentCommentRepository commentRepository;
    private final ApprovalDocumentRepository documentRepository;
    private final DocumentPermissionService permissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentCommentResponse>>> getComments(@PathVariable Long documentId) {
        Long userId = AuthContext.getCurrentUserId();
        
        ApprovalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        
        if (!permissionService.canViewDocument(document, userId)) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("문서 조회 권한이 없습니다."));
        }

        List<DocumentCommentResponse> comments = commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("댓글 목록을 조회했습니다.", comments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentCommentResponse>> createComment(
            @PathVariable Long documentId,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = AuthContext.getCurrentUserId();
        
        ApprovalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        
        if (!permissionService.canViewDocument(document, userId)) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("댓글 작성 권한이 없습니다."));
        }

        DocumentComment comment = DocumentComment.builder()
                .content(request.getContent())
                .authorId(userId)
                .document(document)
                .build();

        DocumentComment savedComment = commentRepository.save(comment);
        DocumentCommentResponse response = convertToResponse(savedComment);

        return ResponseEntity.ok(ApiResponse.success("댓글을 작성했습니다.", response));
    }

    private DocumentCommentResponse convertToResponse(DocumentComment comment) {
        DocumentCommentResponse response = new DocumentCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setAuthorId(comment.getAuthorId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}