package com.hermes.multitenancy.datasource;

import com.hermes.multitenancy.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

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
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getCurrentTenantId();
        log.debug("Determining data source for tenant: {}", tenantId);
        return tenantId;
    }

    @Override
    protected javax.sql.DataSource determineTargetDataSource() {
        String tenantId = TenantContext.getCurrentTenantId();
        
        try {
            javax.sql.DataSource dataSource = dataSourceProvider.getDataSource(tenantId);
            log.debug("Selected data source for tenant: {}", tenantId);
            return dataSource;
        } catch (Exception e) {
            log.error("Failed to get data source for tenant: {}, falling back to default", tenantId, e);
            return dataSourceProvider.getDataSource(TenantContext.DEFAULT_TENANT_ID);
        }
    }
}
