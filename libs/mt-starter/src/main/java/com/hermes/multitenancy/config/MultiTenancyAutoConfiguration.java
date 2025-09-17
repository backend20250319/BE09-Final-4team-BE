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
}
