package com.hermes.userservice.dto;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainProfileResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private WorkPolicyResponseDto workPolicy;
}