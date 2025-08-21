package com.hermes.tenantservice.service.impl;

import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.entity.Tenant;
import com.hermes.multitenancy.util.TenantUtils;
import com.hermes.tenantservice.dto.SchemaInfo;
import com.hermes.tenantservice.repository.TenantManagementRepository;
import com.hermes.tenantservice.service.SchemaManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 스키마 관리 서비스 구현체 (메타데이터 중심)
 * 실제 스키마 작업은 각 서비스에서 RabbitMQ 이벤트를 통해 처리됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchemaManagementServiceImpl implements SchemaManagementService {

    private final TenantManagementRepository tenantRepository;

    @Override
    @Transactional
    public SchemaInfo createSchema(String tenantId, boolean overwrite) {
        log.info("스키마 생성 요청 (메타데이터): tenantId={}, overwrite={}", tenantId, overwrite);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            log.info("스키마 생성은 각 서비스에서 RabbitMQ 이벤트를 통해 처리됩니다: schemaName={}", schemaName);
            
            // 메타데이터 기반 스키마 정보 반환 (실제 생성은 각 서비스에서 이벤트로 처리)
            return SchemaInfo.created(schemaName, 0, List.of());
        });
    }

    @Override
    public SchemaInfo getSchemaInfo(String tenantId) {
        log.debug("스키마 정보 조회 (메타데이터): tenantId={}", tenantId);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 메타데이터 기반 정보 반환 (실제 스키마는 각 서비스에 분산됨)
            log.debug("스키마는 각 서비스에 분산되어 관리됩니다: schemaName={}", schemaName);
            return SchemaInfo.exists(schemaName, 0, List.of("분산 관리됨"));
        });
    }

    @Override
    @Transactional
    public void deleteSchema(String tenantId) {
        log.warn("스키마 삭제 요청 (메타데이터): tenantId={}", tenantId);

        TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 기본 스키마 삭제 방지
            if (TenantContext.DEFAULT_SCHEMA_NAME.equals(schemaName)) {
                throw new IllegalArgumentException("기본 스키마는 삭제할 수 없습니다: " + schemaName);
            }

            log.warn("스키마 삭제는 각 서비스에서 RabbitMQ 이벤트를 통해 처리됩니다: schemaName={}", schemaName);
            return null;
        });
    }

    @Override
    @Transactional
    public SchemaInfo initializeSchema(String tenantId, boolean clearData) {
        log.info("스키마 초기화 요청 (메타데이터): tenantId={}, clearData={}", tenantId, clearData);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            log.info("스키마 초기화는 각 서비스에서 별도로 처리됩니다: schemaName={}", schemaName);
            return SchemaInfo.created(schemaName, 0, List.of("초기화 요청됨"));
        });
    }

    @Override
    public String createBackup(String tenantId) {
        log.info("스키마 백업 생성 요청: tenantId={}", tenantId);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            // 백업 ID 생성 (타임스탬프 기반)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupId = String.format("%s_backup_%s", tenantId, timestamp);

            log.info("백업은 각 서비스에서 개별적으로 처리해야 합니다: tenantId={}, backupId={}", tenantId, backupId);
            
            return backupId;
        });
    }

    @Override
    public SchemaInfo validateSchema(String tenantId) {
        log.debug("스키마 유효성 검증 요청: tenantId={}", tenantId);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            log.debug("스키마 유효성 검증은 각 서비스에서 개별적으로 처리됩니다: schemaName={}", schemaName);
            return SchemaInfo.exists(schemaName, 0, List.of("검증 필요"));
        });
    }

    @Override
    public boolean schemaExists(String tenantId) {
        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElse(null);

            if (tenant == null) {
                return false;
            }

            // 메타데이터 기반으로 존재 여부 반환 (실제 스키마는 각 서비스에 분산됨)
            log.debug("스키마 존재 여부는 각 서비스별로 확인이 필요합니다: schemaName={}", tenant.getSchemaName());
            return true; // 테넌트 메타데이터가 존재하면 스키마도 존재한다고 가정
        });
    }

    /**
     * 시스템 컨텍스트용 기본 테넌트 정보 반환
     */
    private TenantInfo getSystemTenantInfo() {
        return new TenantInfo(
            TenantContext.DEFAULT_TENANT_ID,
            "System",
            TenantContext.DEFAULT_SCHEMA_NAME,
            "ACTIVE"
        );
    }
}
