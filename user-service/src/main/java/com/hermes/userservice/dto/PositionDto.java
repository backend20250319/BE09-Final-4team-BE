package com.hermes.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "직책 응답 DTO")
public class PositionDto {
    
    @Schema(description = "직책 ID")
    private Long id;
    
    @Schema(description = "직책명")
    private String name;
    
    @Schema(description = "정렬 순서")
    private Integer sortOrder;
}