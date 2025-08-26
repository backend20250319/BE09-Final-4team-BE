package com.hermes.userservice.mapper;

import com.hermes.userservice.dto.UserCreateDto;
import com.hermes.userservice.dto.UserResponseDto;
import com.hermes.userservice.dto.UserUpdateDto;
import com.hermes.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

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
                .build();
    }
}