package com.hermes.orgservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {
    
    private Long organizationId;
    private String name;
    private Long parentId;
    private String parentName;
    private List<OrganizationDto> children;
    private int memberCount;
    private int leaderCount;
    
    public OrganizationDto(Long organizationId, String name) {
        this.organizationId = organizationId;
        this.name = name;
    }
    
    public OrganizationDto(Long organizationId, String name, Long parentId, String parentName) {
        this.organizationId = organizationId;
        this.name = name;
        this.parentId = parentId;
        this.parentName = parentName;
    }
}
