package com.hermes.userservice.mapper;

import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.dto.UserUpdateDto;
import com.hermes.userservice.dto.UserOrganizationDto;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.entity.UserOrganization;
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

    public User toEntity(UserCreateDto userCreateDto) {
        return User.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .password(passwordEncoder.encode(userCreateDto.getPassword()))
                .phone(userCreateDto.getPhone())
                .address(userCreateDto.getAddress())
                .joinDate(Optional.ofNullable(userCreateDto.getJoinDate()).orElse(LocalDate.now()))
                .isAdmin(Optional.ofNullable(userCreateDto.getIsAdmin()).orElse(false))
                .needsPasswordReset(Optional.ofNullable(userCreateDto.getNeedsPasswordReset()).orElse(false))
                .employmentType(userCreateDto.getEmploymentType())
                .rank(userCreateDto.getRank())
                .position(userCreateDto.getPosition())
                .job(userCreateDto.getJob())
                .role(userCreateDto.getRole())
                .workPolicyId(userCreateDto.getWorkPolicyId())
                .build();
    }

    public UserResponseDto toResponseDto(User user) {
        List<UserOrganizationDto> organizations = user.getUserOrganizations().stream()
                .map(this::toUserOrganizationDto)
                .collect(Collectors.toList());

        return buildUserResponseDto(user, organizations);
    }
    
    public UserResponseDto toResponseDto(User user, List<Map<String, Object>> remoteOrganizations) {
        List<UserOrganizationDto> organizations = remoteOrganizations.stream()
                .map(this::mapToUserOrganizationDto)
                .collect(Collectors.toList());

        return buildUserResponseDto(user, organizations);
    }
    
    private UserResponseDto buildUserResponseDto(User user, List<UserOrganizationDto> organizations) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .joinDate(user.getJoinDate())
                .isAdmin(user.getIsAdmin())
                .needsPasswordReset(user.getNeedsPasswordReset())
                .employmentType(user.getEmploymentType())
                .rank(user.getRank())
                .position(user.getPosition())
                .job(user.getJob())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .selfIntroduction(user.getSelfIntroduction())
                .workPolicyId(user.getWorkPolicyId())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .organizations(organizations)
                .build();
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