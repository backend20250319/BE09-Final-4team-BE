package com.hermes.userservice.dto;

import com.hermes.userservice.entity.EmploymentType;
import com.hermes.userservice.entity.Job;
import com.hermes.userservice.entity.Position;
import com.hermes.userservice.entity.Rank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate joinDate;
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
    private List<UserOrganizationDto> organizations;
}
