package com.hermes.multitenancy.flyway;

import com.hermes.multitenancy.config.FlywayProperties;
import com.hermes.multitenancy.util.SchemaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 테넌트별 Flyway Migration 실행기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlywayTenantInitializer {
    
    private final DataSource dataSource;
    private final SchemaUtils schemaUtils;
    private final FlywayProperties flywayProperties;
    
    /**
     * 새 테넌트 스키마 생성 및 Migration 실행
     */
    public void initializeTenantSchema(String tenantId, String schemaName) {
        try {
            log.info("Initializing tenant schema: {} for tenant: {}", schemaName, tenantId);
            
            // 1. 스키마 생성
            schemaUtils.createSchema(schemaName);
            log.info("Schema created: {}", schemaName);
            
            // 2. Flyway migration 실행
            if (flywayProperties.isEnabled()) {
                runFlywayMigration(schemaName);
                log.info("Flyway migration completed for schema: {}", schemaName);
            } else {
                log.info("Flyway is disabled, skipping migration for schema: {}", schemaName);
            }
            
        } catch (Exception e) {
            log.error("Failed to initialize tenant schema: {} for tenant: {}", schemaName, tenantId, e);
            throw new RuntimeException("Tenant schema initialization failed", e);
        }
    }
    
    /**
     * 테넌트 스키마 삭제
     */
    public void dropTenantSchema(String tenantId, String schemaName) {
        try {
            log.info("Dropping tenant schema: {} for tenant: {}", schemaName, tenantId);
            
            // 스키마 삭제
            schemaUtils.dropSchema(schemaName);
            log.info("Schema dropped: {}", schemaName);
            
        } catch (Exception e) {
            log.error("Failed to drop tenant schema: {} for tenant: {}", schemaName, tenantId, e);
            throw new RuntimeException("Tenant schema deletion failed", e);
        }
    }
    
    /**
     * 특정 스키마에 대해 Flyway Migration 실행
     */
    private void runFlywayMigration(String schemaName) {
        log.debug("Running Flyway migration for schema: {}", schemaName);
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)  // 특정 스키마 대상
                .locations(flywayProperties.getLocations().toArray(new String[0]))
                .table(flywayProperties.getTable())
                .baselineVersion(flywayProperties.getBaselineVersion())
                .baselineDescription(flywayProperties.getBaselineDescription())
                .baselineOnMigrate(flywayProperties.isBaselineOnMigrate())
                .validateOnMigrate(flywayProperties.isValidateOnMigrate())
                .cleanOnValidationError(flywayProperties.isCleanOnValidationError())
                .executeInTransaction(flywayProperties.isExecuteInTransaction())
                .load();
        
        // Migration 실행
        int migrationsExecuted = flyway.migrate().migrationsExecuted;
        log.info("Executed {} migrations for schema: {}", migrationsExecuted, schemaName);
    }
    
    /**
     * 스키마의 Migration 상태 확인
     */
    public boolean isMigrationRequired(String schemaName) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations(flywayProperties.getLocations().toArray(new String[0]))
                    .table(flywayProperties.getTable())
                    .load();
                    
            return flyway.info().pending().length > 0;
            
        } catch (Exception e) {
            log.warn("Could not check migration status for schema: {}", schemaName, e);
            return true; // 확인할 수 없으면 migration 필요하다고 가정
        }
    }
    
    /**
     * 스키마 Migration 상태 정보 로깅
     */
    public void logMigrationInfo(String schemaName) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations(flywayProperties.getLocations().toArray(new String[0]))
                    .table(flywayProperties.getTable())
                    .load();
                    
            var info = flyway.info();
            log.info("Migration info for schema '{}': {} applied, {} pending", 
                    schemaName, info.applied().length, info.pending().length);
                    
        } catch (Exception e) {
            log.warn("Could not retrieve migration info for schema: {}", schemaName, e);
        }
    }
}
