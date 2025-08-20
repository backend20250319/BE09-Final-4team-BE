package com.hermes.multitenancy.context;

import com.hermes.multitenancy.dto.TenantInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 테넌트 컨텍스트 관리 클래스
 * ThreadLocal을 사용하여 현재 요청의 테넌트 정보를 관리
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<TenantInfo> tenantHolder = new ThreadLocal<>();
    
    /**
     * 기본 테넌트 ID (테넌트 정보가 없을 때 사용)
     */
    public static final String DEFAULT_TENANT_ID = "default";
    public static final String DEFAULT_SCHEMA_NAME = "public";

    /**
     * 현재 스레드의 테넌트 정보 설정
     */
    public static void setTenant(TenantInfo tenantInfo) {
        if (tenantInfo == null) {
            log.warn("Attempting to set null tenant info");
            return;
        }
        
        log.debug("Setting tenant context: {}", tenantInfo.getTenantId());
        tenantHolder.set(tenantInfo);
    }

    /**
     * 현재 스레드의 테넌트 정보 반환
     */
    public static TenantInfo getTenant() {
        TenantInfo tenant = tenantHolder.get();
        if (tenant == null) {
            log.debug("No tenant context found, returning default");
            return getDefaultTenant();
        }
        return tenant;
    }

    /**
     * 현재 테넌트 ID 반환
     */
    public static String getCurrentTenantId() {
        return getTenant().getTenantId();
    }

    /**
     * 현재 테넌트의 스키마명 반환
     */
    public static String getCurrentSchemaName() {
        return getTenant().getSchemaName();
    }

    /**
     * 테넌트 컨텍스트 정리
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        tenantHolder.remove();
    }

    /**
     * 테넌트 정보가 설정되어 있는지 확인
     */
    public static boolean hasTenant() {
        return tenantHolder.get() != null;
    }

    /**
     * 기본 테넌트 정보 반환
     */
    private static TenantInfo getDefaultTenant() {
        return new TenantInfo(DEFAULT_TENANT_ID, "Default", DEFAULT_SCHEMA_NAME, "ACTIVE");
    }

    /**
     * 테넌트 설정과 함께 작업 실행
     */
    public static <T> T executeWithTenant(TenantInfo tenantInfo, TenantOperation<T> operation) {
        TenantInfo previousTenant = tenantHolder.get();
        try {
            setTenant(tenantInfo);
            return operation.execute();
        } finally {
            if (previousTenant != null) {
                setTenant(previousTenant);
            } else {
                clear();
            }
        }
    }

    /**
     * 테넌트 컨텍스트에서 실행할 작업의 인터페이스
     */
    @FunctionalInterface
    public interface TenantOperation<T> {
        T execute();
    }
}
