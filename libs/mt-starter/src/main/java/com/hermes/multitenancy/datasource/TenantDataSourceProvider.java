package com.hermes.multitenancy.datasource;

import com.hermes.multitenancy.context.TenantContext;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 테넌트별 DataSource 제공자
 * Schema-per-tenant 방식으로 각 테넌트마다 별도의 스키마를 사용
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
public class TenantDataSourceProvider {

    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    private DataSource defaultDataSource;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @PostConstruct
    public void initDefaultDataSource() {
        log.info("Initializing default data source");
        defaultDataSource = createDataSource(TenantContext.DEFAULT_SCHEMA_NAME);
        dataSourceMap.put(TenantContext.DEFAULT_TENANT_ID, defaultDataSource);
    }

    /**
     * 현재 테넌트의 DataSource 반환
     */
    public DataSource getCurrentDataSource() {
        String tenantId = TenantContext.getCurrentTenantId();
        return getDataSource(tenantId);
    }

    /**
     * 특정 테넌트의 DataSource 반환
     */
    public DataSource getDataSource(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            log.debug("No tenant ID provided, returning default data source");
            return defaultDataSource;
        }

        return dataSourceMap.computeIfAbsent(tenantId, this::createDataSourceForTenant);
    }

    /**
     * 테넌트용 DataSource 생성
     */
    private DataSource createDataSourceForTenant(String tenantId) {
        log.info("Creating data source for tenant: {}", tenantId);
        
        // 테넌트 ID를 기반으로 스키마명 생성 (예: tenant_company1)
        String schemaName = generateSchemaName(tenantId);
        return createDataSource(schemaName);
    }

    /**
     * 스키마명을 사용하여 DataSource 생성
     */
    private DataSource createDataSource(String schemaName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // 연결 시 스키마 설정
        config.setConnectionInitSql("SET search_path TO " + schemaName);
        
        // 연결 풀 설정
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // 연결 풀 이름 설정
        config.setPoolName("HikariPool-" + schemaName);

        return new HikariDataSource(config);
    }

    /**
     * 테넌트 ID로부터 스키마명 생성
     */
    private String generateSchemaName(String tenantId) {
        if (TenantContext.DEFAULT_TENANT_ID.equals(tenantId)) {
            return TenantContext.DEFAULT_SCHEMA_NAME;
        }
        
        // 스키마명은 tenant_ 접두사를 사용 (예: tenant_company1)
        return "tenant_" + tenantId.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }

    /**
     * 테넌트 DataSource 제거 (테넌트 삭제 시 사용)
     */
    public void removeTenantDataSource(String tenantId) {
        log.info("Removing data source for tenant: {}", tenantId);
        
        DataSource dataSource = dataSourceMap.remove(tenantId);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    /**
     * 모든 DataSource 정리
     */
    public void closeAllDataSources() {
        log.info("Closing all data sources");
        
        dataSourceMap.values().forEach(dataSource -> {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        });
        dataSourceMap.clear();
    }
}
