package com.hermes.approvalservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {
    
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String content;
}