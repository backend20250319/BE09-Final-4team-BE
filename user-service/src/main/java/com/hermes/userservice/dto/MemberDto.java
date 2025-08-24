package com.hermes.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate joinDate;
    private Boolean isAdmin;
    private String role;
    private String profileImage;
    private String selfIntroduction;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String employmentType;
    private String rank;
    private String position;
    private String job;

    private List<String> organizations;
    private String mainOrganization;
    private Integer memberCount;
    private Integer leaderCount;
}
