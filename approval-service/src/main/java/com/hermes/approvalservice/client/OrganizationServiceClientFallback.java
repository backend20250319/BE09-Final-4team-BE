package com.hermes.approvalservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class OrganizationServiceClientFallback implements OrganizationServiceClient {

    @Override
    public OrganizationInfo getOrganizationById(Long id) {
        log.warn("Organization service is unavailable. Returning fallback for organization id: {}", id);
        OrganizationInfo fallback = new OrganizationInfo();
        fallback.setId(id);
        fallback.setName("Unknown Organization");
        return fallback;
    }

    @Override
    public List<Long> getOrganizationMembers(Long id) {
        log.warn("Organization service is unavailable. Returning empty members list for organization id: {}", id);
        return Collections.emptyList();
    }

    @Override
    public List<ManagerInfo> getOrganizationManagers(Long id) {
        log.warn("Organization service is unavailable. Returning empty managers list for organization id: {}", id);
        return Collections.emptyList();
    }

    @Override
    public ManagerInfo getNthLevelManager(Long userId, Integer level) {
        log.warn("Organization service is unavailable. Returning fallback manager for user {} level {}", userId, level);
        ManagerInfo fallback = new ManagerInfo();
        fallback.setUserId(userId);
        fallback.setName("Unknown Manager");
        fallback.setLevel(level);
        return fallback;
    }
}