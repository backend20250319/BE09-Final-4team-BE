package com.hermes.multitenancy.service;

import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.entity.Tenant;

/**
 * 테넌트 서비스 인터페이스
 */
public interface TenantService {

    /**
     * 테넌트 ID로 테넌트 정보 조회
     */
    TenantInfo getTenantInfo(String tenantId);

    /**
     * 사용자 이메일로 테넌트 정보 조회
     */
    TenantInfo getTenantInfoByUserEmail(String email);

    /**
     * 테넌트 등록
     */
    Tenant createTenant(String tenantId, String name, String adminEmail);

    /**
     * 테넌트 존재 여부 확인
     */
    boolean existsTenant(String tenantId);

    /**
     * 테넌트 스키마 초기화
     */
    void initializeTenantSchema(String tenantId);
}
