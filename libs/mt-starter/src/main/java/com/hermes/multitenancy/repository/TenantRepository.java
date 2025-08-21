package com.hermes.multitenancy.repository;

import com.hermes.multitenancy.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 테넌트 리포지토리
 * 기본 스키마(public)에서 테넌트 메타데이터를 관리
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * 테넌트 ID로 테넌트 조회
     */
    Optional<Tenant> findByTenantId(String tenantId);

    /**
     * 테넌트 ID 존재 여부 확인
     */
    boolean existsByTenantId(String tenantId);


    /**
     * 관리자 이메일로 테넌트 조회
     */
    Optional<Tenant> findByAdminEmail(String adminEmail);
}
