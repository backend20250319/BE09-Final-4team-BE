package com.hermes.userserviceclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDto {
    private UserResponseDto user;
    private WorkPolicyResponseDto workPolicy;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponseDto {
        private Long id;
        private String email;
        private String name;
        private String profileUrl;
        private String department;
        private String position;
        private String employeeNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkPolicyResponseDto {
        private Long id;
        private String name;
        private String description;
    }
} 