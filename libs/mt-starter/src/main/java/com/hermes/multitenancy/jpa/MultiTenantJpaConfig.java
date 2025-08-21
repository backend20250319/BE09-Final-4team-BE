package com.hermes.multitenancy.jpa;

import com.hermes.multitenancy.datasource.TenantDataSourceProvider;
import com.hermes.multitenancy.datasource.TenantRoutingDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * 멀티테넌트 JPA 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(
    basePackages = {"${hermes.multitenancy.repository-packages:com.hermes}"},
    entityManagerFactoryRef = "multiTenantEntityManagerFactory",
    transactionManagerRef = "multiTenantTransactionManager"
)
public class MultiTenantJpaConfig {

    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;

    @Bean
    @Primary
    public DataSource multiTenantDataSource(TenantDataSourceProvider dataSourceProvider) {
        log.info("Creating multi-tenant routing data source");
        return new TenantRoutingDataSource(dataSourceProvider);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean multiTenantEntityManagerFactory(
            DataSource multiTenantDataSource) {
        
        log.info("Creating multi-tenant entity manager factory");
        
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(multiTenantDataSource);
        
        // Hibernate Vendor Adapter 설정
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        
        // 엔티티 패키지 스캔 설정
        factoryBean.setPackagesToScan(getEntityPackages());
        
        // Hibernate 속성 설정
        factoryBean.setJpaPropertyMap(
            hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), 
                new HibernateSettings()
            )
        );
        
        return factoryBean;
    }

    @Bean
    @Primary
    public PlatformTransactionManager multiTenantTransactionManager(
            EntityManagerFactory multiTenantEntityManagerFactory) {
        
        log.info("Creating multi-tenant transaction manager");
        return new JpaTransactionManager(multiTenantEntityManagerFactory);
    }

    /**
     * 엔티티 스캔 패키지 목록 반환
     */
    private String[] getEntityPackages() {
        return new String[]{
            "com.hermes.multitenancy.entity",
            "com.hermes.**.entity"  // 모든 hermes 패키지의 entity를 포함
        };
    }
}
