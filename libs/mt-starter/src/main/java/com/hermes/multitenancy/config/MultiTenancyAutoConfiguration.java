package com.hermes.multitenancy.config;

import com.hermes.multitenancy.hibernate.SchemaBasedConnectionProvider;
import com.hermes.multitenancy.hibernate.TenantIdentifierResolver;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * 멀티테넌시 자동 설정
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MultiTenancyProperties.class)
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.hermes.multitenancy")
public class MultiTenancyAutoConfiguration {

    @Bean
    @Primary
    public HibernatePropertiesCustomizer multiTenantHibernatePropertiesCustomizer(
            SchemaBasedConnectionProvider connectionProvider,
            TenantIdentifierResolver tenantResolver) {

        log.info("Configuring Hibernate multitenancy with SchemaBasedConnectionProvider and TenantIdentifierResolver");

        return (Map<String, Object> hibernateProperties) -> {
            hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
            hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);

            log.debug("Hibernate multitenancy properties configured: connection_provider={}, identifier_resolver={}",
                connectionProvider.getClass().getSimpleName(), tenantResolver.getClass().getSimpleName());
        };
    }
}
