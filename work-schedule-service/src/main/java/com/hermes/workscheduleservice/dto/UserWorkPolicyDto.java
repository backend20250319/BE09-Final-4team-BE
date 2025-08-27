package com.hermes.workscheduleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkPolicyDto {
    private Long workPolicyId;
    private WorkPolicyDto workPolicy;
} 