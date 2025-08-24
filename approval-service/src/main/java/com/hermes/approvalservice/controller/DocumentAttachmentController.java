package com.hermes.approvalservice.controller;

import com.hermes.approvalservice.dto.ApiResponse;
import com.hermes.approvalservice.dto.response.DocumentAttachmentResponse;
import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentAttachment;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.repository.ApprovalDocumentRepository;
import com.hermes.approvalservice.repository.DocumentAttachmentRepository;
import com.hermes.approvalservice.service.DocumentPermissionService;
import com.hermes.jwt.context.AuthContext;
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
public class DocumentAttachmentController {

    private final DocumentAttachmentRepository attachmentRepository;
    private final ApprovalDocumentRepository documentRepository;
    private final DocumentPermissionService permissionService;
    
    private static final String UPLOAD_DIR = "uploads/approval-attachments/";

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentAttachmentResponse>>> getAttachments(@PathVariable Long documentId) {
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

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentAttachmentResponse>> uploadAttachment(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file) {
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

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long documentId,
            @PathVariable Long fileId) {
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