package com.hermes.userservice.dto;

import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponseDto {
    private UserResponseDto user;
    private WorkPolicyResponseDto workPolicy;
}
