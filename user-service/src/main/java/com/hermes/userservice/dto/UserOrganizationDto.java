package com.hermes.userservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrganizationDto {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Boolean isPrimary;
    private Boolean isLeader;
    private LocalDateTime assignedAt;
}
