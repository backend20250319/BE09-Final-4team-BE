package com.hermes.tenantservice.service;

import com.hermes.tenantservice.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * 테넌트 관리 서비스 인터페이스
 */
public interface TenantManagementService {

    /**
     * 테넌트 생성
     */
    TenantResponse createTenant(CreateTenantRequest request);

    /**
     * 테넌트 목록 조회 (페이징)
     */
    PagedResponse<TenantResponse> getTenants(Pageable pageable);

    /**
     * 특정 테넌트 조회
     */
    TenantResponse getTenant(String tenantId);

    /**
     * 테넌트 정보 수정
     */
    TenantResponse updateTenant(String tenantId, UpdateTenantRequest request);

    /**
     * 테넌트 삭제
     */
    void deleteTenant(String tenantId, boolean deleteSchema);

    /**
     * 테넌트 상태 변경
     */
    TenantResponse updateTenantStatus(String tenantId, String status);

    /**
     * 테넌트 존재 여부 확인
     */
    boolean existsTenant(String tenantId);
}
