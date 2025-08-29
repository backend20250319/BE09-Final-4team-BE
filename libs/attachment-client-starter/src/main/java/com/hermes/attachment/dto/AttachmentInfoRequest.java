package com.hermes.attachment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttachmentInfoRequest {
    
    @NotBlank(message = "파일 ID는 필수입니다")
    private String fileId;
    
    @NotBlank(message = "표시할 파일명은 필수입니다")
    private String displayFileName;
}