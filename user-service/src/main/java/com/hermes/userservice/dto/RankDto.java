package com.hermes.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "직급 응답 DTO")
public class RankDto {
    
    @Schema(description = "직급 ID")
    private Long id;
    
    @Schema(description = "직급명")
    private String name;
    
    @Schema(description = "정렬 순서")
    private Integer sortOrder;
}