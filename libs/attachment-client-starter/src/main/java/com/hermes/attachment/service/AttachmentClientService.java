package com.hermes.attachment.service;

import com.hermes.attachment.client.AttachmentServiceClient;
import com.hermes.attachment.dto.AttachmentInfoResponse;
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
    
    public List<AttachmentInfo> validateAndConvertAttachments(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        
        return fileIds.stream()
                .map(this::validateAndConvertAttachment)
                .collect(Collectors.toList());
    }
    
    public AttachmentInfo validateAndConvertAttachment(String fileId) {
        try {
            AttachmentInfoResponse metadata = attachmentServiceClient.getFileMetadata(fileId);
            
            log.info("File metadata validated for fileId: {}, size: {}, type: {}", 
                    fileId, metadata.getFileSize(), metadata.getContentType());
            
            return AttachmentInfo.builder()
                    .fileId(fileId)
                    .fileName(metadata.getFileName())
                    .fileSize(metadata.getFileSize())
                    .contentType(metadata.getContentType())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to validate attachment: {}", fileId, e);
            throw new RuntimeException("첨부파일 검증에 실패했습니다: " + fileId, e);
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
        response.setFileName(attachment.getFileName());
        response.setFileSize(attachment.getFileSize());
        response.setContentType(attachment.getContentType());
        return response;
    }
}