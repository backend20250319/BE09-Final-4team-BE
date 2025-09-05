package com.hermes.userservice.dto;

import com.hermes.userservice.entity.EmploymentType;
import com.hermes.userservice.entity.Job;
import com.hermes.userservice.entity.Position;
import com.hermes.userservice.entity.Rank;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 응답 DTO")
public class UserResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    
    private Boolean isAdmin;
    private Boolean needsPasswordReset;
    private EmploymentType employmentType;
    private Rank rank;
    private Position position;
    private Job job;
    private String role;
    private String profileImageUrl;
    private String selfIntroduction;
    private Long workPolicyId;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private WorkPolicyResponseDto workPolicy;
    private List<UserOrganizationDto> organizations;
}
