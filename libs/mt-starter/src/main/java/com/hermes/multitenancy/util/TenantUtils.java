package com.hermes.multitenancy.util;

import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 테넌트 관련 유틸리티 클래스
 */
@Slf4j
public class TenantUtils {

    /**
     * 현재 테넌트가 기본 테넌트인지 확인
     */
    public static boolean isDefaultTenant() {
        return TenantContext.DEFAULT_TENANT_ID.equals(TenantContext.getCurrentTenantId());
    }

    /**
     * 테넌트 ID 유효성 검사
     */
    public static boolean isValidTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return false;
        }
        
        // 테넌트 ID는 영숫자와 하이픈, 언더스코어만 허용
        return tenantId.matches("^[a-zA-Z0-9_-]+$") && tenantId.length() <= 50;
    }

    /**
     * 스키마명 유효성 검사
     */
    public static boolean isValidSchemaName(String schemaName) {
        if (schemaName == null || schemaName.trim().isEmpty()) {
            return false;
        }
        
        // PostgreSQL 스키마명 규칙에 따라 검증
        return schemaName.matches("^[a-z][a-z0-9_]*$") && schemaName.length() <= 63;
    }

    /**
     * 테넌트 ID를 스키마명으로 변환
     */
    public static String convertToSchemaName(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return TenantContext.DEFAULT_SCHEMA_NAME;
        }
        
        if (TenantContext.DEFAULT_TENANT_ID.equals(tenantId)) {
            return TenantContext.DEFAULT_SCHEMA_NAME;
        }
        
        return "tenant_" + tenantId.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }

    /**
     * 이메일에서 도메인 추출
     */
    public static String extractDomainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        
        String domain = email.substring(email.indexOf("@") + 1);
        return domain.toLowerCase();
    }

    /**
     * 도메인을 테넌트 ID로 변환
     */
    public static String convertDomainToTenantId(String domain) {
        if (domain == null || domain.isEmpty()) {
            return TenantContext.DEFAULT_TENANT_ID;
        }
        
        // 최상위 도메인 제거 (예: company.com -> company)
        String tenantId = domain.replaceAll("\\.[^.]+$", "");
        
        // 특수 문자를 밑줄로 변경
        tenantId = tenantId.replaceAll("[^a-zA-Z0-9]", "_");
        
        // 유효하지 않은 경우 기본 테넌트 반환
        if (!isValidTenantId(tenantId)) {
            return TenantContext.DEFAULT_TENANT_ID;
        }
        
        return tenantId.toLowerCase();
    }

    /**
     * 테넌트별 리소스 경로 생성
     */
    public static String getTenantResourcePath(String basePath) {
        String tenantId = TenantContext.getCurrentTenantId();
        
        if (isDefaultTenant()) {
            return basePath;
        }
        
        return basePath + "/" + tenantId;
    }

    /**
     * 테넌트별 캐시 키 생성
     */
    public static String getTenantCacheKey(String baseKey) {
        String tenantId = TenantContext.getCurrentTenantId();
        return tenantId + ":" + baseKey;
    }

    /**
     * 테넌트별 로그 컨텍스트 설정
     */
    public static void setLogContext() {
        String tenantId = TenantContext.getCurrentTenantId();
        org.slf4j.MDC.put("tenantId", tenantId);
        
        TenantInfo tenantInfo = TenantContext.getTenant();
        if (tenantInfo != null) {
            org.slf4j.MDC.put("schemaName", tenantInfo.getSchemaName());
        }
    }

    /**
     * 로그 컨텍스트 정리
     */
    public static void clearLogContext() {
        org.slf4j.MDC.remove("tenantId");
        org.slf4j.MDC.remove("schemaName");
    }

    /**
     * 테넌트 정보를 문자열로 포맷
     */
    public static String formatTenantInfo(TenantInfo tenantInfo) {
        if (tenantInfo == null) {
            return "null";
        }
        
        return String.format(
            "Tenant{id='%s', name='%s', schema='%s', status='%s'}",
            tenantInfo.getTenantId(),
            tenantInfo.getName(),
            tenantInfo.getSchemaName(),
            tenantInfo.getStatus()
        );
    }
}
