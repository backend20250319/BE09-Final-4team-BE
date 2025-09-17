package com.hermes.multitenancy.hibernate;

import com.hermes.multitenancy.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Hibernate가 현재 테넌트를 식별하기 위한 Resolver
 * TenantContext에서 현재 테넌트 ID를 반환
 */
@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        try {
            if (!TenantContext.hasTenantContext()) {
                log.debug("No tenant context found");
                return "";
            }

            String tenantId = TenantContext.getCurrentTenantId();
            log.debug("Resolved tenant identifier: {}", tenantId);
            return tenantId;

        } catch (Exception e) {
            log.warn("Failed to resolve tenant identifier", e);
            return "";
        }
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // 기존 세션 유효성 검증 활성화
        // 테넌트가 변경되면 기존 세션을 새로 생성하도록 함
        return true;
    }
}