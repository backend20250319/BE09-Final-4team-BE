package com.hermes.userservice.dto.title;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePositionRequest {
    @NotBlank(message = "직위명은 필수입니다")
    @Size(max = 50, message = "직위명은 50자를 초과할 수 없습니다")
    private String name;
    
    private Integer sortOrder;
}
