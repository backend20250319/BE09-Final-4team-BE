package com.hermes.orgservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationRequest {
    
    @NotBlank(message = "조직명은 필수입니다.")
    @Size(min = 1, max = 100, message = "조직명은 1자 이상 100자 이하여야 합니다.")
    private String name;
    
    private Long parentId;
}
