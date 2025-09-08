package com.hermes.userservice.mapper;

import com.hermes.userservice.dto.*;
import com.hermes.userservice.dto.title.*;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.entity.UserOrganization;
import com.hermes.userservice.repository.*;
import com.hermes.userservice.util.CareerCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final RankRepository rankRepository;
    private final PositionRepository positionRepository;
    private final JobRepository jobRepository;

    public User toEntity(UserCreateDto userCreateDto) {
        LocalDate joinDate = Optional.ofNullable(userCreateDto.getJoinDate()).orElse(LocalDate.now());
        int workYears = CareerCalculator.calculateCareerYears(joinDate);
        
        return User.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .password(passwordEncoder.encode(userCreateDto.getPassword()))
                .phone(userCreateDto.getPhone())
                .address(userCreateDto.getAddress())
                .joinDate(joinDate)
                .workYears(workYears)
                .isAdmin(Optional.ofNullable(userCreateDto.getIsAdmin()).orElse(false))
                .needsPasswordReset(Optional.ofNullable(userCreateDto.getNeedsPasswordReset()).orElse(false))
                .employmentType(convertToEmploymentTypeEntity(userCreateDto.getEmploymentType()))
                .rank(convertToRankEntity(userCreateDto.getRank()))
                .position(convertToPositionEntity(userCreateDto.getPosition()))
                .job(convertToJobEntity(userCreateDto.getJob()))
                .role(userCreateDto.getRole())
                .workPolicyId(userCreateDto.getWorkPolicyId())
                .build();
    }

    public UserResponseDto toResponseDto(User user) {
        List<UserOrganizationDto> organizations = user.getUserOrganizations().stream()
                .map(this::toUserOrganizationDto)
                .collect(Collectors.toList());

        return buildUserResponseDto(user, organizations, null);
    }
    
    public UserResponseDto toResponseDto(User user, List<Map<String, Object>> remoteOrganizations) {
        List<UserOrganizationDto> organizations = remoteOrganizations.stream()
                .map(this::mapToUserOrganizationDto)
                .collect(Collectors.toList());

        return buildUserResponseDto(user, organizations, null);
    }

    public UserResponseDto toResponseDto(User user, List<Map<String, Object>> remoteOrganizations, WorkPolicyResponseDto workPolicy) {
        List<UserOrganizationDto> organizations = remoteOrganizations.stream()
                .map(this::mapToUserOrganizationDto)
                .collect(Collectors.toList());

        return buildUserResponseDto(user, organizations, workPolicy);
    }

    public MainProfileResponseDto toMainProfileDto(User user, WorkPolicyResponseDto workPolicy) {
        return MainProfileResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .workPolicy(workPolicy)
                .rank(user.getRank() != null ? user.getRank().getName() : null)
                .position(user.getPosition() != null ? user.getPosition().getName() : null)
                .job(user.getJob() != null ? user.getJob().getName() : null)
                .workPolicyId(user.getWorkPolicyId())
                .build();
    }

    public DetailProfileResponseDto toDetailProfileDto(User user) {
        return DetailProfileResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .address(user.getAddress())
                .joinDate(user.getJoinDate())
                .build();
    }
    
    private UserResponseDto buildUserResponseDto(User user, List<UserOrganizationDto> organizations, WorkPolicyResponseDto workPolicy) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isAdmin(user.getIsAdmin())
                .needsPasswordReset(user.getNeedsPasswordReset())
                .employmentType(convertToEmploymentTypeDto(user.getEmploymentType()))
                .rank(user.getRank() != null ? user.getRank().getName() : null)
                .position(user.getPosition() != null ? user.getPosition().getName() : null)
                .job(user.getJob() != null ? user.getJob().getName() : null)
                .rankDto(convertToRankDto(user.getRank()))
                .positionDto(convertToPositionDto(user.getPosition()))
                .jobDto(convertToJobDto(user.getJob()))
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .selfIntroduction(user.getSelfIntroduction())
                .workPolicyId(user.getWorkPolicyId())
                .workYears(user.getWorkYears())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .workPolicy(workPolicy)
                .organizations(organizations)
                .build();
    }
    
    private EmploymentTypeDto convertToEmploymentTypeDto(com.hermes.userservice.entity.EmploymentType employmentType) {
        if (employmentType == null) {
            return null;
        }
        return EmploymentTypeDto.builder()
                .id(employmentType.getId())
                .name(employmentType.getName())
                .sortOrder(employmentType.getSortOrder())
                .build();
    }
    
    private RankDto convertToRankDto(com.hermes.userservice.entity.Rank rank) {
        if (rank == null) {
            return null;
        }
        return RankDto.builder()
                .id(rank.getId())
                .name(rank.getName())
                .sortOrder(rank.getSortOrder())
                .build();
    }
    
    private PositionDto convertToPositionDto(com.hermes.userservice.entity.Position position) {
        if (position == null) {
            return null;
        }
        return PositionDto.builder()
                .id(position.getId())
                .name(position.getName())
                .sortOrder(position.getSortOrder())
                .build();
    }
    
    private JobDto convertToJobDto(com.hermes.userservice.entity.Job job) {
        if (job == null) {
            return null;
        }
        return JobDto.builder()
                .id(job.getId())
                .name(job.getName())
                .sortOrder(job.getSortOrder())
                .build();
    }
    
    private com.hermes.userservice.entity.EmploymentType convertToEmploymentTypeEntity(EmploymentTypeDto employmentTypeDto) {
        if (employmentTypeDto == null || employmentTypeDto.getId() == null || employmentTypeDto.getId() == 0) {
            return null;
        }
        return employmentTypeRepository.findById(employmentTypeDto.getId()).orElse(null);
    }
    
    private com.hermes.userservice.entity.Rank convertToRankEntity(RankDto rankDto) {
        if (rankDto == null || rankDto.getId() == null || rankDto.getId() == 0) {
            return null;
        }
        return rankRepository.findById(rankDto.getId()).orElse(null);
    }
    
    private com.hermes.userservice.entity.Position convertToPositionEntity(PositionDto positionDto) {
        if (positionDto == null || positionDto.getId() == null || positionDto.getId() == 0) {
            return null;
        }
        return positionRepository.findById(positionDto.getId()).orElse(null);
    }
    
    private com.hermes.userservice.entity.Job convertToJobEntity(JobDto jobDto) {
        if (jobDto == null || jobDto.getId() == null || jobDto.getId() == 0) {
            return null;
        }
        return jobRepository.findById(jobDto.getId()).orElse(null);
    }
    
    public UserOrganizationDto toUserOrganizationDto(UserOrganization userOrganization) {
        return UserOrganizationDto.builder()
                .id(userOrganization.getId())
                .organizationId(userOrganization.getOrganizationId())
                .organizationName(userOrganization.getOrganizationName())
                .isPrimary(userOrganization.getIsPrimary())
                .isLeader(userOrganization.getIsLeader())
                .assignedAt(userOrganization.getAssignedAt())
                .build();
    }
    
    public UserOrganizationDto mapToUserOrganizationDto(Map<String, Object> remoteData) {
        return UserOrganizationDto.builder()
                .id(((Number) remoteData.get("assignmentId")).longValue())
                .organizationId(((Number) remoteData.get("organizationId")).longValue())
                .organizationName((String) remoteData.get("organizationName"))
                .isPrimary((Boolean) remoteData.get("isPrimary"))
                .isLeader((Boolean) remoteData.get("isLeader"))
                .assignedAt(remoteData.get("assignedAt") != null ? 
                        java.time.LocalDateTime.parse((String) remoteData.get("assignedAt")) : null)
                .build();
    }
}