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
@RequestMapping("/api/approval/documents/{documentId}/comments")
@RequiredArgsConstructor
@Tag(name = "문서 댓글 관리", description = "결재 문서의 댓글 조회, 작성 API")
public class DocumentCommentController {

    private final DocumentCommentRepository commentRepository;
    private final ApprovalDocumentRepository documentRepository;
    private final DocumentPermissionService permissionService;

    @Operation(summary = "문서 댓글 목록 조회", description = "지정한 문서의 댓글 목록을 시간순으로 조회합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
            @SwaggerApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @SwaggerApiResponse(responseCode = "403", description = "문서 조회 권한이 없습니다"),
            @SwaggerApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @SwaggerApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentCommentResponse>>> getComments(
            @Parameter(description = "문서 ID", required = true) @PathVariable Long documentId) {
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

    @Operation(summary = "문서 댓글 작성", description = "지정한 문서에 새로운 댓글을 작성합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @SwaggerApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @SwaggerApiResponse(responseCode = "403", description = "댓글 작성 권한이 없습니다"),
            @SwaggerApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @SwaggerApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<DocumentCommentResponse>> createComment(
            @Parameter(description = "문서 ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "댓글 작성 요청 정보", required = true) @Valid @RequestBody CreateCommentRequest request) {
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