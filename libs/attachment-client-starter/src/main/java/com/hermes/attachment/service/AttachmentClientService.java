package com.hermes.attachment.service;

import com.hermes.attachment.client.AttachmentServiceClient;
import com.hermes.attachment.dto.AttachmentInfoRequest;
import com.hermes.attachment.dto.AttachmentInfoResponse;
import com.hermes.attachment.dto.AttachmentMetadata;
import com.hermes.attachment.entity.AttachmentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentClientService {
    
    private final AttachmentServiceClient attachmentServiceClient;
    
    public List<AttachmentInfo> validateAndConvertAttachments(List<AttachmentInfoRequest> attachmentRequests) {
        if (attachmentRequests == null || attachmentRequests.isEmpty()) {
            return List.of();
        }
        
        return attachmentRequests.stream()
                .map(this::validateAndConvertAttachment)
                .collect(Collectors.toList());
    }
    
    public AttachmentInfo validateAndConvertAttachment(AttachmentInfoRequest request) {
        try {
            AttachmentMetadata metadata = attachmentServiceClient.getFileMetadata(request.getFileId());
            
            log.info("File metadata validated for fileId: {}, size: {}, type: {}", 
                    request.getFileId(), metadata.getFileSize(), metadata.getContentType());
            
            return AttachmentInfo.builder()
                    .fileId(request.getFileId())
                    .displayFileName(sanitizeFileName(request.getDisplayFileName()))
                    .fileSize(metadata.getFileSize())
                    .contentType(metadata.getContentType())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to validate attachment: {}", request.getFileId(), e);
            throw new RuntimeException("첨부파일 검증에 실패했습니다: " + request.getFileId());
        }
    }
    
    public List<AttachmentInfoResponse> convertToResponseList(List<AttachmentInfo> attachments) {
        return attachments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public AttachmentInfoResponse convertToResponse(AttachmentInfo attachment) {
        AttachmentInfoResponse response = new AttachmentInfoResponse();
        response.setFileId(attachment.getFileId());
        response.setDisplayFileName(attachment.getDisplayFileName());
        response.setFileSize(attachment.getFileSize());
        response.setContentType(attachment.getContentType());
        response.setDownloadUrl("/api/attachments/" + attachment.getFileId() + "/download");
        return response;
    }
    
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "Unknown";
        }
        return fileName.replaceAll("[<>\"'&]", "_").trim();
    }
}