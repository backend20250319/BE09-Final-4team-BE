package com.hermes.multitenancy.util;

import com.hermes.multitenancy.context.TenantContext;

/**
 * 테넌트 관련 유틸리티 클래스
 * 
 * 규칙: 소문자로 시작, 소문자+숫자+언더스코어만 허용 (a-z0-9_)
 * 최대 길이: TenantId(50자), SchemaName(63자)
 */
public class TenantUtils {

    private static final String TENANT_SCHEMA_PREFIX = "tenant_";
    private static final String IDENTIFIER_PATTERN = "^[a-z][a-z0-9_]*$";
    private static final int MAX_TENANT_ID_LENGTH = 50;
    private static final int MAX_SCHEMA_NAME_LENGTH = 63;

    /**
     * 식별자 유효성 검사 (테넌트 ID 및 스키마명)
     */
    private static boolean isValidIdentifier(String identifier, int maxLength) {
        return identifier != null && 
               !identifier.trim().isEmpty() && 
               identifier.matches(IDENTIFIER_PATTERN) && 
               identifier.length() <= maxLength;
    }

    /**
     * 테넌트 ID 유효성 검사
     */
    public static boolean isValidTenantId(String tenantId) {
        return isValidIdentifier(tenantId, MAX_TENANT_ID_LENGTH);
    }

    /**
     * 스키마명 유효성 검사
     */
    public static boolean isValidSchemaName(String schemaName) {
        return isValidIdentifier(schemaName, MAX_SCHEMA_NAME_LENGTH);
    }

    /**
     * 테넌트 ID로부터 스키마명 생성
     */
    public static String generateSchemaName(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return null; // NonTenant인 경우 스키마명 없음
        }
        return TENANT_SCHEMA_PREFIX + tenantId;
    }
}
