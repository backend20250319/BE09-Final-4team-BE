package com.hermes.multitenancy.service;

import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.entity.Tenant;
import com.hermes.multitenancy.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 테넌트 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    
    // 메모리 캐시를 위한 맵 (운영 환경에서는 Redis 등 외부 캐시 사용 권장)
    private final Map<String, TenantInfo> tenantCache = new ConcurrentHashMap<>();
    private final Map<String, String> emailToTenantCache = new ConcurrentHashMap<>();

    @Override
    @Cacheable(value = "tenants", key = "#tenantId")
    public TenantInfo getTenantInfo(String tenantId) {
        log.debug("Getting tenant info for: {}", tenantId);
        
        // 캐시에서 확인
        TenantInfo cached = tenantCache.get(tenantId);
        if (cached != null) {
            return cached;
        }

        // 기본 테넌트인 경우
        if (TenantContext.DEFAULT_TENANT_ID.equals(tenantId)) {
            TenantInfo defaultTenant = new TenantInfo(
                TenantContext.DEFAULT_TENANT_ID,
                "Default Tenant",
                TenantContext.DEFAULT_SCHEMA_NAME,
                "ACTIVE"
            );
            tenantCache.put(tenantId, defaultTenant);
            return defaultTenant;
        }

        // 데이터베이스에서 조회 (기본 스키마에서)
        return TenantContext.executeWithTenant(
            new TenantInfo(TenantContext.DEFAULT_TENANT_ID, null, TenantContext.DEFAULT_SCHEMA_NAME, "ACTIVE"),
            () -> {
                return tenantRepository.findByTenantId(tenantId)
                    .map(tenant -> {
                        TenantInfo info = new TenantInfo(tenant);
                        tenantCache.put(tenantId, info);
                        return info;
                    })
                    .orElse(null);
            }
        );
    }

    @Override
    public TenantInfo getTenantInfoByUserEmail(String email) {
        log.debug("Getting tenant info by user email: {}", email);
        
        // 이메일에서 도메인 추출하여 테넌트 ID 유추
        String domain = extractDomain(email);
        String inferredTenantId = convertDomainToTenantId(domain);
        
        // 캐시에서 확인
        String cachedTenantId = emailToTenantCache.get(email);
        if (cachedTenantId != null) {
            return getTenantInfo(cachedTenantId);
        }

        // 추론된 테넌트 ID로 조회
        TenantInfo tenantInfo = getTenantInfo(inferredTenantId);
        if (tenantInfo != null) {
            emailToTenantCache.put(email, inferredTenantId);
            return tenantInfo;
        }

        // 기본 테넌트 반환
        log.warn("No tenant found for email: {}, returning default tenant", email);
        return getTenantInfo(TenantContext.DEFAULT_TENANT_ID);
    }

    @Override
    @Transactional
    public Tenant createTenant(String tenantId, String name, String adminEmail) {
        log.info("Creating new tenant: {}", tenantId);
        
        // 기본 스키마에서 테넌트 생성
        return TenantContext.executeWithTenant(
            new TenantInfo(TenantContext.DEFAULT_TENANT_ID, null, TenantContext.DEFAULT_SCHEMA_NAME, "ACTIVE"),
            () -> {
                String schemaName = generateSchemaName(tenantId);
                
                Tenant tenant = new Tenant(tenantId, name, schemaName);
                tenant.setAdminEmail(adminEmail);
                
                Tenant savedTenant = tenantRepository.save(tenant);
                
                // 캐시 갱신
                TenantInfo tenantInfo = new TenantInfo(savedTenant);
                tenantCache.put(tenantId, tenantInfo);
                
                return savedTenant;
            }
        );
    }

    @Override
    public boolean existsTenant(String tenantId) {
        if (TenantContext.DEFAULT_TENANT_ID.equals(tenantId)) {
            return true;
        }
        
        return TenantContext.executeWithTenant(
            new TenantInfo(TenantContext.DEFAULT_TENANT_ID, null, TenantContext.DEFAULT_SCHEMA_NAME, "ACTIVE"),
            () -> tenantRepository.existsByTenantId(tenantId)
        );
    }

    @Override
    public void initializeTenantSchema(String tenantId) {
        log.info("Initializing schema for tenant: {}", tenantId);
        
        // 스키마 초기화 로직은 각 서비스에서 구현
        // 여기서는 기본적인 로그만 남김
        TenantInfo tenantInfo = getTenantInfo(tenantId);
        if (tenantInfo != null) {
            log.info("Schema {} is ready for tenant {}", tenantInfo.getSchemaName(), tenantId);
        }
    }

    /**
     * 이메일에서 도메인 추출
     */
    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) {
            return "default";
        }
        return email.substring(email.indexOf("@") + 1);
    }

    /**
     * 도메인을 테넌트 ID로 변환
     */
    private String convertDomainToTenantId(String domain) {
        return domain.replaceAll("\\.[^.]+$", "").replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * 테넌트 ID로부터 스키마명 생성
     */
    private String generateSchemaName(String tenantId) {
        if (TenantContext.DEFAULT_TENANT_ID.equals(tenantId)) {
            return TenantContext.DEFAULT_SCHEMA_NAME;
        }
        return "tenant_" + tenantId.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }
}
