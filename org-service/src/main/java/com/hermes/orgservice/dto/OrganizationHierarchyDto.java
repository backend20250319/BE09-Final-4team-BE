package com.hermes.orgservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationHierarchyDto {
    
    private Long organizationId;
    private String name;
    private Long parentId;
    private String parentName;
    private List<OrganizationHierarchyDto> children;
    private int memberCount;
    private int leaderCount;
    private Boolean isExpanded;
    
    public OrganizationHierarchyDto(Long organizationId, String name) {
        this.organizationId = organizationId;
        this.name = name;
        this.isExpanded = false;
    }
    
    public OrganizationHierarchyDto(Long organizationId, String name, Long parentId, String parentName) {
        this.organizationId = organizationId;
        this.name = name;
        this.parentId = parentId;
        this.parentName = parentName;
        this.isExpanded = false;
    }
}
