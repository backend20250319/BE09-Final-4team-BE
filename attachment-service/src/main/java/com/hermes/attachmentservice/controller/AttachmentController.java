package com.hermes.attachmentservice.controller;

import com.hermes.attachmentservice.service.AttachmentService;
import com.hermes.attachment.dto.AttachmentInfoResponse;
import com.hermes.api.common.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.hermes.auth.principal.UserPrincipal;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {
    
    private final AttachmentService attachmentService;
    
    // 파일 업로드 (인증된 사용자면 가능)
    @PostMapping("/upload")
    public ResponseEntity<ApiResult<List<AttachmentInfoResponse>>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserPrincipal user) {
        
        log.info("파일 업로드 요청 - 파일 수: {}, 업로더: {}", files.size(), user.getUserId());
        
        try {
            List<AttachmentInfoResponse> response = attachmentService.uploadFiles(files, user.getUserId());
            return ResponseEntity.ok(ApiResult.success(response));
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResult.failure(e.getMessage()));
        }
    }

    // 파일 다운로드
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        log.info("파일 다운로드 요청: {}", fileId);
        
        try {
            // 메타데이터 조회
            AttachmentInfoResponse metadata = attachmentService.getFileMetadata(fileId);
            
            // 파일 리소스 조회
            Resource resource = attachmentService.getFileResource(fileId);
            
            // 파일명 인코딩 (한글 파일명 지원)
            String encodedFileName = UriUtils.encode(metadata.getFileName(), StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .contentLength(metadata.getFileSize())
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // 파일 삭제 (ADMIN 권한 필요)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResult<Void>> deleteFile(@PathVariable String fileId) {
        log.info("파일 삭제 요청: {}", fileId);
        
        try {
            attachmentService.deleteFile(fileId);
            return ResponseEntity.ok(ApiResult.success());
            
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResult.failure(e.getMessage()));
        }
    }
    
    // 파일 정보 조회
    @GetMapping("/{fileId}/info")
    public ResponseEntity<ApiResult<AttachmentInfoResponse>> getFileMetadata(@PathVariable String fileId) {
        log.info("파일 정보 조회: {}", fileId);
        
        try {
            AttachmentInfoResponse response = attachmentService.getFileMetadata(fileId);
            return ResponseEntity.ok(ApiResult.success(response));
            
        } catch (Exception e) {
            log.error("파일 정보 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResult.failure(e.getMessage()));
        }
    }

}