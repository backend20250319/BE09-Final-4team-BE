package com.hermes.approvalservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "org-service", fallback = OrganizationServiceClientFallback.class)
public interface OrganizationServiceClient {

    @GetMapping("/api/organizations/{id}")
    OrganizationInfo getOrganizationById(@PathVariable("id") Long id);

    @GetMapping("/api/organizations/{id}/members")
    List<Long> getOrganizationMembers(@PathVariable("id") Long id);

    @GetMapping("/api/organizations/{id}/managers")
    List<ManagerInfo> getOrganizationManagers(@PathVariable("id") Long id);

    @GetMapping("/api/users/{userId}/manager/{level}")
    ManagerInfo getNthLevelManager(@PathVariable("userId") Long userId, @PathVariable("level") Integer level);

    class OrganizationInfo {
        private Long id;
        private String name;
        private Long parentId;

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
    }

    class ManagerInfo {
        private Long userId;
        private String name;
        private Integer level;
        private Long organizationId;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    }
}