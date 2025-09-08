package com.hermes.userservice.dto;

import com.hermes.userservice.dto.title.EmploymentTypeDto;
import com.hermes.userservice.dto.title.JobDto;
import com.hermes.userservice.dto.title.PositionDto;
import com.hermes.userservice.dto.title.RankDto;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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
    private EmploymentTypeDto employmentType;
    
    private String rank;
    private String position;
    private String job;
    
    private RankDto rankDto;
    private PositionDto positionDto;
    private JobDto jobDto;
    
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
