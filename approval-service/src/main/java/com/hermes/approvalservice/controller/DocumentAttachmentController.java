package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.approvalservice.dto.response.DocumentAttachmentResponse;
import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentAttachment;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.repository.ApprovalDocumentRepository;
import com.hermes.approvalservice.repository.DocumentAttachmentRepository;
import com.hermes.approvalservice.service.DocumentPermissionService;
import com.hermes.auth.context.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/approval/documents/{documentId}/attachments")
@RequiredArgsConstructor
@Tag(name = "문서 첨부파일 관리", description = "결재 문서의 첨부파일 업로드, 조회, 다운로드 API")
public class DocumentAttachmentController {

    private final DocumentAttachmentRepository attachmentRepository;
    private final ApprovalDocumentRepository documentRepository;
    private final DocumentPermissionService permissionService;
    
    private static final String UPLOAD_DIR = "uploads/approval-attachments/";

    @Operation(summary = "문서 첨부파일 목록 조회", description = "지정한 문서의 첨부파일 목록을 조회합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "첨부파일 목록 조회 성공"),
            @SwaggerApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @SwaggerApiResponse(responseCode = "403", description = "문서 조회 권한이 없습니다"),
            @SwaggerApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @SwaggerApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentAttachmentResponse>>> getAttachments(
            @Parameter(description = "문서 ID", required = true) @PathVariable Long documentId) {
        Long userId = AuthContext.getCurrentUserId();
        
        ApprovalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        
        if (!permissionService.canViewDocument(document, userId)) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("문서 조회 권한이 없습니다."));
        }

        List<DocumentAttachmentResponse> attachments = attachmentRepository.findByDocumentId(documentId)
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("첨부파일 목록을 조회했습니다.", attachments));
    }

    @Operation(summary = "문서 첨부파일 업로드", description = "문서에 첨부파일을 업로드합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "파일 업로드 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
            @SwaggerApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @SwaggerApiResponse(responseCode = "403", description = "파일 업로드 권한이 없습니다"),
            @SwaggerApiResponse(responseCode = "404", description = "문서를 찾을 수 없습니다"),
            @SwaggerApiResponse(responseCode = "500", description = "파일 업로드 중 오류가 발생했습니다")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<DocumentAttachmentResponse>> uploadAttachment(
            @Parameter(description = "문서 ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "업로드할 파일", required = true) @RequestParam("file") MultipartFile file) {
        Long userId = AuthContext.getCurrentUserId();
        
        ApprovalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        
        if (!permissionService.canEditDocument(document, userId)) {
            return ResponseEntity.status(403).body(ApiResponse.rejected("파일 업로드 권한이 없습니다."));
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique file name
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String storedFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(storedFileName);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Save attachment info to database
            DocumentAttachment attachment = DocumentAttachment.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedBy(userId)
                    .document(document)
                    .build();

            DocumentAttachment savedAttachment = attachmentRepository.save(attachment);
            DocumentAttachmentResponse response = convertToResponse(savedAttachment);

            return ResponseEntity.ok(ApiResponse.success("파일을 업로드했습니다.", response));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.failure("파일 업로드 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "첨부파일 다운로드", description = "지정한 첨부파일을 다운로드합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @SwaggerApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @SwaggerApiResponse(responseCode = "403", description = "파일 다운로드 권한이 없습니다"),
            @SwaggerApiResponse(responseCode = "404", description = "파일을 찾을 수 없습니다"),
            @SwaggerApiResponse(responseCode = "500", description = "파일 다운로드 중 오류가 발생했습니다")
    })
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "문서 ID", required = true) @PathVariable Long documentId,
            @Parameter(description = "파일 ID", required = true) @PathVariable Long fileId) {
        Long userId = AuthContext.getCurrentUserId();
        
        ApprovalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));
        
        if (!permissionService.canViewDocument(document, userId)) {
            return ResponseEntity.status(403).build();
        }

        DocumentAttachment attachment = attachmentRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("첨부파일을 찾을 수 없습니다."));

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(attachment.getContentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private DocumentAttachmentResponse convertToResponse(DocumentAttachment attachment) {
        DocumentAttachmentResponse response = new DocumentAttachmentResponse();
        response.setId(attachment.getId());
        response.setOriginalFileName(attachment.getOriginalFileName());
        response.setStoredFileName(attachment.getStoredFileName());
        response.setFileSize(attachment.getFileSize());
        response.setContentType(attachment.getContentType());
        response.setUploadedBy(attachment.getUploadedBy());
        response.setCreatedAt(attachment.getCreatedAt());
        return response;
    }
}