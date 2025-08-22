package com.hermes.multitenancy.config;

import com.hermes.multitenancy.datasource.TenantDataSourceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 멀티테넌시 자동 설정
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MultiTenancyProperties.class)
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.hermes.multitenancy")
public class MultiTenancyAutoConfiguration {

    public MultiTenancyAutoConfiguration() {
        log.info("Enabling Hermes Multi-tenancy Auto Configuration");
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantDataSourceProvider tenantDataSourceProvider() {
        log.info("Creating TenantDataSourceProvider bean");
        return new TenantDataSourceProvider();
    }

    // 다른 빈들은 @Component 어노테이션으로 자동 스캔되므로 여기서는 DataSourceProvider만 생성
}
