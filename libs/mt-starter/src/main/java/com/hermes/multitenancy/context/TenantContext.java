package com.hermes.multitenancy.context;

import lombok.extern.slf4j.Slf4j;

/**
 * 테넌트 컨텍스트 관리 클래스
 * ThreadLocal을 사용하여 현재 요청의 테넌트 정보를 관리
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> tenantIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isNonTenantHolder = new ThreadLocal<>();

    /**
     * 현재 스레드에 테넌트 ID 설정
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("Attempting to set null or empty tenant ID");
            return;
        }

        log.debug("Setting tenant context: {}", tenantId);
        tenantIdHolder.set(tenantId);
        isNonTenantHolder.set(false);
    }

    /**
     * 현재 스레드에 NonTenant 컨텍스트 설정
     */
    public static void setNonTenant() {
        log.debug("Setting non-tenant context");
        tenantIdHolder.remove();
        isNonTenantHolder.set(true);
    }

    /**
     * 현재 테넌트 ID 반환
     * NonTenant 컨텍스트인 경우 예외 발생
     */
    public static String getCurrentTenantId() {
        if (isNonTenant()) {
            throw new IllegalStateException("NonTenant 컨텍스트에서는 tenantId를 사용할 수 없습니다");
        }

        String tenantId = tenantIdHolder.get();
        if (tenantId == null) {
            throw new IllegalStateException("테넌트 컨텍스트가 설정되지 않았습니다");
        }

        return tenantId;
    }

    /**
     * 현재 컨텍스트가 NonTenant인지 확인
     */
    public static boolean isNonTenant() {
        return Boolean.TRUE.equals(isNonTenantHolder.get());
    }

    /**
     * 테넌트 컨텍스트가 설정되어 있는지 확인
     */
    public static boolean hasTenantContext() {
        return isNonTenant() || tenantIdHolder.get() != null;
    }

    /**
     * 테넌트 컨텍스트 정리
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        tenantIdHolder.remove();
        isNonTenantHolder.remove();
    }

    /**
     * 테넌트 설정과 함께 작업 실행
     */
    public static <T> T executeWithTenant(String tenantId, TenantOperation<T> operation) {
        String previousTenantId = tenantIdHolder.get();
        Boolean previousIsNonTenant = isNonTenantHolder.get();

        try {
            setTenantId(tenantId);
            return operation.execute();
        } finally {
            if (previousTenantId != null) {
                setTenantId(previousTenantId);
            } else if (Boolean.TRUE.equals(previousIsNonTenant)) {
                setNonTenant();
            } else {
                clear();
            }
        }
    }

    /**
     * NonTenant 설정과 함께 작업 실행
     */
    public static <T> T executeWithNonTenant(TenantOperation<T> operation) {
        String previousTenantId = tenantIdHolder.get();
        Boolean previousIsNonTenant = isNonTenantHolder.get();

        try {
            setNonTenant();
            return operation.execute();
        } finally {
            if (previousTenantId != null) {
                setTenantId(previousTenantId);
            } else if (Boolean.TRUE.equals(previousIsNonTenant)) {
                setNonTenant();
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
