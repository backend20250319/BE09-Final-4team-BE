package com.hermes.tenantservice.service.impl;

import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.multitenancy.entity.Tenant;
import com.hermes.multitenancy.util.SchemaUtils;
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
 * 스키마 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchemaManagementServiceImpl implements SchemaManagementService {

    private final TenantManagementRepository tenantRepository;
    private final SchemaUtils schemaUtils;

    @Override
    @Transactional
    public SchemaInfo createSchema(String tenantId, boolean overwrite) {
        log.info("스키마 생성 시작: tenantId={}, overwrite={}", tenantId, overwrite);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 스키마 존재 여부 확인
            boolean schemaExists = schemaUtils.schemaExists(schemaName);
            
            if (schemaExists && !overwrite) {
                // 이미 존재하는 경우 정보 반환
                int tableCount = schemaUtils.getSchemaTableCount(schemaName);
                List<String> tables = schemaUtils.getSchemaTableNames(schemaName);
                log.info("스키마가 이미 존재함: schemaName={}, tableCount={}", schemaName, tableCount);
                return SchemaInfo.exists(schemaName, tableCount, tables);
            }

            // 기존 스키마 삭제 (덮어쓰기 모드)
            if (schemaExists && overwrite) {
                log.warn("기존 스키마 삭제: schemaName={}", schemaName);
                if (!schemaUtils.dropSchema(schemaName)) {
                    throw new RuntimeException("기존 스키마 삭제에 실패했습니다: " + schemaName);
                }
            }

            // 새 스키마 생성
            if (!schemaUtils.createSchema(schemaName)) {
                throw new RuntimeException("스키마 생성에 실패했습니다: " + schemaName);
            }

            // 스키마 초기화 (기본 테이블들 생성)
            initializeSchemaStructure(tenantId, schemaName);

            // 생성된 스키마 정보 조회
            int tableCount = schemaUtils.getSchemaTableCount(schemaName);
            List<String> tables = schemaUtils.getSchemaTableNames(schemaName);

            log.info("스키마 생성 완료: schemaName={}, tableCount={}", schemaName, tableCount);
            return SchemaInfo.created(schemaName, tableCount, tables);
        });
    }

    @Override
    public SchemaInfo getSchemaInfo(String tenantId) {
        log.debug("스키마 정보 조회: tenantId={}", tenantId);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 스키마 존재 여부 확인
            if (!schemaUtils.schemaExists(schemaName)) {
                return SchemaInfo.notExists(schemaName);
            }

            // 스키마 정보 조회
            int tableCount = schemaUtils.getSchemaTableCount(schemaName);
            List<String> tables = schemaUtils.getSchemaTableNames(schemaName);

            return SchemaInfo.exists(schemaName, tableCount, tables);
        });
    }

    @Override
    @Transactional
    public void deleteSchema(String tenantId) {
        log.warn("스키마 삭제 시작: tenantId={}", tenantId);

        TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 기본 스키마 삭제 방지
            if (TenantContext.DEFAULT_SCHEMA_NAME.equals(schemaName)) {
                throw new IllegalArgumentException("기본 스키마는 삭제할 수 없습니다: " + schemaName);
            }

            // 스키마 존재 여부 확인
            if (!schemaUtils.schemaExists(schemaName)) {
                log.warn("삭제할 스키마가 존재하지 않음: schemaName={}", schemaName);
                return null;
            }

            // 스키마 삭제
            if (!schemaUtils.dropSchema(schemaName)) {
                throw new RuntimeException("스키마 삭제에 실패했습니다: " + schemaName);
            }

            log.warn("스키마 삭제 완료: schemaName={}", schemaName);
            return null;
        });
    }

    @Override
    @Transactional
    public SchemaInfo initializeSchema(String tenantId, boolean clearData) {
        log.info("스키마 초기화 시작: tenantId={}, clearData={}", tenantId, clearData);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 스키마 존재 여부 확인
            if (!schemaUtils.schemaExists(schemaName)) {
                throw new IllegalArgumentException("스키마가 존재하지 않습니다: " + schemaName);
            }

            // 기존 데이터 삭제
            if (clearData) {
                log.warn("스키마 데이터 삭제: schemaName={}", schemaName);
                if (!schemaUtils.clearSchema(schemaName)) {
                    throw new RuntimeException("스키마 데이터 삭제에 실패했습니다: " + schemaName);
                }
            }

            // 스키마 구조 초기화
            initializeSchemaStructure(tenantId, schemaName);

            // 초기화된 스키마 정보 조회
            int tableCount = schemaUtils.getSchemaTableCount(schemaName);
            List<String> tables = schemaUtils.getSchemaTableNames(schemaName);

            log.info("스키마 초기화 완료: schemaName={}, tableCount={}", schemaName, tableCount);
            return SchemaInfo.created(schemaName, tableCount, tables);
        });
    }

    @Override
    public String createBackup(String tenantId) {
        log.info("스키마 백업 생성 시작: tenantId={}", tenantId);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 스키마 존재 여부 확인
            if (!schemaUtils.schemaExists(schemaName)) {
                throw new IllegalArgumentException("스키마가 존재하지 않습니다: " + schemaName);
            }

            // 백업 ID 생성 (타임스탬프 기반)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupId = String.format("%s_backup_%s", tenantId, timestamp);

            // TODO: 실제 백업 로직 구현 (pg_dump 등 사용)
            log.info("백업 생성 완료: tenantId={}, backupId={}", tenantId, backupId);
            
            return backupId;
        });
    }

    @Override
    public SchemaInfo validateSchema(String tenantId) {
        log.debug("스키마 유효성 검증 시작: tenantId={}", tenantId);

        return TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            // 테넌트 조회
            Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

            String schemaName = tenant.getSchemaName();

            // 스키마 존재 여부 확인
            if (!schemaUtils.schemaExists(schemaName)) {
                return SchemaInfo.error(schemaName, "스키마가 존재하지 않습니다");
            }

            // 스키마 정보 조회
            int tableCount = schemaUtils.getSchemaTableCount(schemaName);
            List<String> tables = schemaUtils.getSchemaTableNames(schemaName);

            // TODO: 추가 유효성 검증 로직 (필수 테이블 존재 여부 등)
            
            log.debug("스키마 유효성 검증 완료: schemaName={}, tableCount={}", schemaName, tableCount);
            return SchemaInfo.exists(schemaName, tableCount, tables);
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

            return schemaUtils.schemaExists(tenant.getSchemaName());
        });
    }

    /**
     * 스키마 구조 초기화 (테이블 생성 등)
     * 실제로는 Flyway나 Liquibase를 사용하는 것이 좋습니다
     */
    private void initializeSchemaStructure(String tenantId, String schemaName) {
        log.info("스키마 구조 초기화: tenantId={}, schemaName={}", tenantId, schemaName);

        // 테넌트별 스키마에서 JPA 엔티티 테이블 생성을 위해
        // 임시로 해당 테넌트 컨텍스트로 전환
        TenantInfo tenantInfo = new TenantInfo(tenantId, tenantId, schemaName, "ACTIVE");
        
        TenantContext.executeWithTenant(tenantInfo, () -> {
            // TODO: 필요한 경우 추가 테이블 생성 로직
            // 예: 기본 데이터 삽입, 인덱스 생성 등
            
            log.debug("스키마 구조 초기화 완료: schemaName={}", schemaName);
            return null;
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
