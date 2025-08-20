package com.hermes.orgservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAssignmentRequest {
    
    @NotNull(message = "직원 ID는 필수입니다.")
    private Long employeeId;
    
    @NotNull(message = "조직 ID는 필수입니다.")
    private Long organizationId;
    
    private Boolean isPrimary = false;
    
    private Boolean isLeader = false;
}
