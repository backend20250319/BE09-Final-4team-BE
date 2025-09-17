package com.hermes.multitenancy.datasource;

import com.hermes.multitenancy.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * 테넌트별 DataSource 라우팅
 * 현재 테넌트 컨텍스트에 따라 적절한 DataSource를 선택
 */
@Slf4j
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final TenantDataSourceProvider dataSourceProvider;

    public TenantRoutingDataSource(TenantDataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
        setLenientFallback(true); // 테넌트를 찾을 수 없을 때 기본 DataSource 사용
    }

    @Override
    public void afterPropertiesSet() {
        // NonTenant용 기본 DataSource 설정
        Map<Object, Object> targetDataSources = new HashMap<>();
        setTargetDataSources(targetDataSources);
        setDefaultTargetDataSource(dataSourceProvider.getDefaultDataSource());

        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        if (TenantContext.isNonTenant()) {
            log.debug("Determining data source for non-tenant context");
            return null; // 기본 DataSource 사용
        }

        String tenantId = TenantContext.getCurrentTenantId();
        log.debug("Determining data source for tenant: {}", tenantId);
        return tenantId;
    }

    @Override
    protected javax.sql.DataSource determineTargetDataSource() {
        try {
            // 컨텍스트가 설정되지 않은 경우 (애플리케이션 초기화 시 등)
            if (!TenantContext.hasTenantContext()) {
                javax.sql.DataSource dataSource = dataSourceProvider.getDefaultDataSource();
                log.debug("Selected default data source (no context)");
                return dataSource;
            }

            if (TenantContext.isNonTenant()) {
                javax.sql.DataSource dataSource = dataSourceProvider.getDefaultDataSource();
                log.debug("Selected default data source for non-tenant context");
                return dataSource;
            }

            String tenantId = TenantContext.getCurrentTenantId();
            javax.sql.DataSource dataSource = dataSourceProvider.getDataSource(tenantId);
            log.debug("Selected data source for tenant: {}", tenantId);
            return dataSource;
        } catch (Exception e) {
            log.error("Failed to get data source, falling back to default", e);
            return dataSourceProvider.getDefaultDataSource();
        }
    }
}
