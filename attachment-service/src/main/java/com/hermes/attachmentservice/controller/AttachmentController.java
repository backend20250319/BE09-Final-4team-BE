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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "파일 첨부 관리", description = "파일 업로드, 다운로드, 삭제 및 메타데이터 조회 API")
public class AttachmentController {
    
    private final AttachmentService attachmentService;
    
    @Operation(summary = "파일 업로드", description = "하나 이상의 파일을 업로드합니다. 인증된 사용자만 사용 가능.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 업로드 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "413", description = "파일 크기 초과"),
        @ApiResponse(responseCode = "415", description = "지원하지 않는 파일 형식"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/upload")
    public ResponseEntity<ApiResult<List<AttachmentInfoResponse>>> uploadFiles(
            @Parameter(description = "업로드할 파일 목록", required = true) @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserPrincipal user) {
        
        log.info("파일 업로드 요청 - 파일 수: {}, 업로더: {}", files.size(), user.getId());
        
        try {
            List<AttachmentInfoResponse> response = attachmentService.uploadFiles(files, user.getId());
            return ResponseEntity.ok(ApiResult.success(response));
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResult.failure(e.getMessage()));
        }
    }

    @Operation(summary = "파일 다운로드", description = "파일 ID를 통해 파일을 다운로드합니다. 한글 파일명도 지원.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "다운로드할 파일 ID", required = true, example = "uuid-file-id") @PathVariable String fileId) {
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
    
    @Operation(summary = "파일 삭제", description = "파일을 삭제합니다. ADMIN 권한 필요.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResult<Void>> deleteFile(
            @Parameter(description = "삭제할 파일 ID", required = true, example = "uuid-file-id") @PathVariable String fileId) {
        log.info("파일 삭제 요청: {}", fileId);
        
        try {
            attachmentService.deleteFile(fileId);
            return ResponseEntity.ok(ApiResult.success());
            
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResult.failure(e.getMessage()));
        }
    }
    
    @Operation(summary = "파일 정보 조회", description = "파일의 메타데이터 정보를 조회합니다. (파일명, 크기, 타입 등)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 정보 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{fileId}/info")
    public ResponseEntity<ApiResult<AttachmentInfoResponse>> getFileMetadata(
            @Parameter(description = "조회할 파일 ID", required = true, example = "uuid-file-id") @PathVariable String fileId) {
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