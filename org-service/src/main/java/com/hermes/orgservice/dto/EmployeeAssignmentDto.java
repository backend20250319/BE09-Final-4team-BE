package com.hermes.orgservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAssignmentDto {
    
    private Long assignmentId;
    private Long employeeId;
    private String employeeName;
    private Long organizationId;
    private String organizationName;
    private Boolean isPrimary;
    private Boolean isLeader;
    private LocalDateTime assignedAt;
}
