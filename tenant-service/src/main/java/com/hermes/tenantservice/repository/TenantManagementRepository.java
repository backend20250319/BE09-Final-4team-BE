package com.hermes.tenantservice.repository;

import com.hermes.multitenancy.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 테넌트 관리 리포지토리
 * 시스템 관리용으로 기본 스키마(public)에서 작동
 */
@Repository
public interface TenantManagementRepository extends JpaRepository<Tenant, Long> {

    /**
     * 테넌트 ID로 테넌트 조회
     */
    Optional<Tenant> findByTenantId(String tenantId);

    /**
     * 테넌트 ID 존재 여부 확인
     */
    boolean existsByTenantId(String tenantId);

    /**
     * 스키마명으로 테넌트 조회
     */
    Optional<Tenant> findBySchemaName(String schemaName);

    /**
     * 스키마명 존재 여부 확인
     */
    boolean existsBySchemaName(String schemaName);

    /**
     * 관리자 이메일로 테넌트 조회
     */
    List<Tenant> findByAdminEmail(String adminEmail);

    /**
     * 활성 상태인 테넌트 목록 조회
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = com.hermes.multitenancy.entity.Tenant.TenantStatus.ACTIVE")
    Page<Tenant> findActiveTenantsBy(Pageable pageable);

    /**
     * 특정 상태의 테넌트 목록 조회
     */
    Page<Tenant> findByStatus(Tenant.TenantStatus status, Pageable pageable);

    /**
     * 테넌트명으로 검색 (Like 검색)
     */
    @Query("SELECT t FROM Tenant t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Tenant> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * 특정 기간에 생성된 테넌트 조회
     */
    @Query("SELECT t FROM Tenant t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<Tenant> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                       @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 전체 테넌트 수 조회
     */
    @Query("SELECT COUNT(t) FROM Tenant t")
    long countAllTenants();

    /**
     * 활성 테넌트 수 조회
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status = com.hermes.multitenancy.entity.Tenant.TenantStatus.ACTIVE")
    long countActiveTenants();
}
