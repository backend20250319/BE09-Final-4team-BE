package com.hermes.userservice.dto;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponseDto {
    private UserResponseDto user;
    private WorkPolicyResponseDto workPolicy;
    private List<Map<String, Object>> organizations;
}
