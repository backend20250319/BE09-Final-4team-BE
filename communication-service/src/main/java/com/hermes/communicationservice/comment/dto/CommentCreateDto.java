package com.hermes.communicationservice.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateDto {
    
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String content;
    
    @NotNull(message = "작성자 ID는 필수입니다")
    private Long authorId;
}
