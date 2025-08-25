package com.hermes.multitenancy.messaging;

import com.hermes.multitenancy.config.RabbitMQProperties;
import com.hermes.events.tenant.TenantEvent;
import com.hermes.multitenancy.flyway.FlywayTenantInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * Flyway 기반 테넌트 이벤트 리스너
 * DefaultTenantEventListener를 대체하는 Flyway 통합 버전
 */
@Slf4j
public class FlywayTenantEventListener extends AbstractTenantEventListener {
    
    private final FlywayTenantInitializer flywayTenantInitializer;
    private final String serviceName;
    
    public FlywayTenantEventListener(RabbitMQProperties properties, 
                                   FlywayTenantInitializer flywayTenantInitializer, 
                                   String serviceName) {
        super(properties);
        this.flywayTenantInitializer = flywayTenantInitializer;
        this.serviceName = serviceName;
    }
    
    @Override
    protected String getServiceName() {
        return serviceName;
    }
    
    @Override
    @RabbitListener(queues = "#{@tenantEventQueue.name}")
    public void handleTenantEvent(TenantEvent event) {
        try {
            log.info("Received tenant event in service '{}': {}", serviceName, event.getEventType());
            
            switch (event.getEventType()) {
                case TENANT_CREATED -> handleTenantCreated(event);
                case TENANT_DELETED -> handleTenantDeleted(event);
                case TENANT_UPDATED -> handleTenantUpdated(event);
                case TENANT_STATUS_CHANGED -> handleTenantStatusChanged(event);
                default -> log.warn("Unhandled tenant event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Failed to handle tenant event in service '{}': {}", serviceName, event, e);
            throw e; // Re-throw for dead letter queue
        }
    }
    
    @Override
    protected void handleTenantCreated(TenantEvent event) {
        log.info("Creating tenant schema with Flyway: {} (tenant: {})", 
                event.getSchemaName(), event.getTenantId());
        
        // Flyway를 사용한 스키마 및 테이블 생성
        flywayTenantInitializer.initializeTenantSchema(event.getTenantId(), event.getSchemaName());
        
        log.info("Successfully created tenant schema with tables: {} (tenant: {})", 
                event.getSchemaName(), event.getTenantId());
    }
    
    @Override
    protected void handleTenantDeleted(TenantEvent event) {
        log.info("Deleting tenant schema: {} (tenant: {})", 
                event.getSchemaName(), event.getTenantId());
        
        // 스키마 삭제
        flywayTenantInitializer.dropTenantSchema(event.getTenantId(), event.getSchemaName());
        
        log.info("Successfully deleted tenant schema: {} (tenant: {})", 
                event.getSchemaName(), event.getTenantId());
    }
    
    @Override
    protected void handleTenantUpdated(TenantEvent event) {
        log.info("Tenant updated: {} (tenant: {})", event.getSchemaName(), event.getTenantId());
        
        // 필요시 스키마 migration 상태 확인 및 업데이트
        if (flywayTenantInitializer.isMigrationRequired(event.getSchemaName())) {
            log.info("Running pending migrations for tenant: {}", event.getTenantId());
            flywayTenantInitializer.initializeTenantSchema(event.getTenantId(), event.getSchemaName());
        } else {
            log.info("No migrations required for tenant: {}", event.getTenantId());
        }
        
        flywayTenantInitializer.logMigrationInfo(event.getSchemaName());
    }
    
    @Override
    protected void handleTenantStatusChanged(TenantEvent event) {
        log.info("Tenant status changed to '{}': {} (tenant: {})", 
                event.getStatus(), event.getSchemaName(), event.getTenantId());
        
        // 상태 변경에 따른 추가 작업이 필요한 경우 구현
        // 예: INACTIVE 상태일 때 스키마 비활성화 등
        
        if ("INACTIVE".equals(event.getStatus())) {
            log.info("Tenant is now inactive, consider cleanup: {}", event.getTenantId());
        } else if ("ACTIVE".equals(event.getStatus())) {
            log.info("Tenant is now active: {}", event.getTenantId());
            // 필요시 migration 상태 확인
            flywayTenantInitializer.logMigrationInfo(event.getSchemaName());
        }
    }
}
