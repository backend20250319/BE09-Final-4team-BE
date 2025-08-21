package com.hermes.multitenancy.util;

import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 테넌트 관련 유틸리티 클래스
 * 
 * TenantId와 SchemaName 통일 규칙:
 * - 소문자로 시작 (a-z)
 * - 소문자, 숫자, 언더스코어만 허용 (a-z0-9_)
 * - 최대 길이: TenantId(50자), SchemaName(63자)
 * - 스키마명은 "tenant_{tenantId}" 패턴으로 생성
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
     * PostgreSQL 스키마명 규칙과 일치하도록 통일
     */
    public static boolean isValidTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return false;
        }
        
        // 스키마명과 동일한 규칙: 소문자로 시작, 소문자+숫자+언더스코어만 허용
        return tenantId.matches("^[a-z][a-z0-9_]*$") && tenantId.length() <= 50;
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
     * TenantId가 스키마명 규칙을 따르므로 단순 접두사 추가
     */
    public static String convertToSchemaName(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return TenantContext.DEFAULT_SCHEMA_NAME;
        }
        
        if (TenantContext.DEFAULT_TENANT_ID.equals(tenantId)) {
            return TenantContext.DEFAULT_SCHEMA_NAME;
        }
        
        // TenantId가 이미 스키마명 규칙을 따르므로 단순히 접두사 추가
        return "tenant_" + tenantId;
    }

    /**
     * 테넌트 ID로부터 스키마명 생성 (convertToSchemaName의 alias)
     */
    public static String generateSchemaName(String tenantId) {
        return convertToSchemaName(tenantId);
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
     * 새로운 TenantId 규칙(소문자로 시작, 소문자+숫자+언더스코어)에 맞게 변환
     */
    public static String convertDomainToTenantId(String domain) {
        if (domain == null || domain.isEmpty()) {
            return TenantContext.DEFAULT_TENANT_ID;
        }
        
        // 최상위 도메인 제거 (예: company.com -> company)
        String tenantId = domain.replaceAll("\\.[^.]+$", "");
        
        // 소문자로 변환 및 유효하지 않은 문자를 언더스코어로 변경
        tenantId = tenantId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        
        // 숫자로 시작하면 앞에 접두사 추가
        if (tenantId.matches("^[0-9].*")) {
            tenantId = "t_" + tenantId;
        }
        
        // 유효하지 않은 경우 기본 테넌트 반환
        if (!isValidTenantId(tenantId)) {
            return TenantContext.DEFAULT_TENANT_ID;
        }
        
        return tenantId;
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
