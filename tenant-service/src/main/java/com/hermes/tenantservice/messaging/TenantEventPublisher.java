package com.hermes.tenantservice.messaging;

import com.hermes.events.tenant.TenantEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 테넌트 이벤트를 RabbitMQ로 발행하는 Publisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${hermes.multitenancy.rabbitmq.tenant-exchange:tenant.events}")
    private String tenantExchange;

    @Value("${hermes.multitenancy.rabbitmq.tenant-created-routing-key:tenant.created}")
    private String tenantCreatedRoutingKey;

    @Value("${hermes.multitenancy.rabbitmq.tenant-deleted-routing-key:tenant.deleted}")
    private String tenantDeletedRoutingKey;

    @Value("${hermes.multitenancy.rabbitmq.tenant-updated-routing-key:tenant.updated}")
    private String tenantUpdatedRoutingKey;

    @Value("${hermes.multitenancy.rabbitmq.tenant-status-changed-routing-key:tenant.status.changed}")
    private String tenantStatusChangedRoutingKey;

    /**
     * 테넌트 생성 이벤트 발행
     */
    public void publishTenantCreated(String tenantId, String tenantName, String adminEmail) {
        TenantEvent event = TenantEvent.created(tenantId, tenantName, adminEmail);
        
        publishEvent(event, tenantCreatedRoutingKey);
        log.info("Tenant CREATED event published: tenantId={}, schemaName={}", tenantId, event.getSchemaName());
    }

    /**
     * 테넌트 삭제 이벤트 발행
     */
    public void publishTenantDeleted(String tenantId) {
        TenantEvent event = TenantEvent.deleted(tenantId);
        
        publishEvent(event, tenantDeletedRoutingKey);
        log.info("Tenant DELETED event published: tenantId={}, schemaName={}", tenantId, event.getSchemaName());
    }

    /**
     * 테넌트 업데이트 이벤트 발행
     */
    public void publishTenantUpdated(String tenantId, String tenantName, String adminEmail) {
        TenantEvent event = new TenantEvent(
                TenantEvent.EventType.TENANT_UPDATED,
                tenantId,
                tenantName,
                "ACTIVE",
                adminEmail,
                java.time.LocalDateTime.now()
        );
        
        publishEvent(event, tenantUpdatedRoutingKey);
        log.info("Tenant UPDATED event published: tenantId={}, schemaName={}", tenantId, event.getSchemaName());
    }

    /**
     * 테넌트 상태 변경 이벤트 발행
     */
    public void publishTenantStatusChanged(String tenantId, String status) {
        TenantEvent event = new TenantEvent(
                TenantEvent.EventType.TENANT_STATUS_CHANGED,
                tenantId,
                null,
                status,
                null,
                java.time.LocalDateTime.now()
        );
        
        publishEvent(event, tenantStatusChangedRoutingKey);
        log.info("Tenant STATUS_CHANGED event published: tenantId={}, schemaName={}, status={}", 
                tenantId, event.getSchemaName(), status);
    }

    /**
     * 이벤트를 RabbitMQ로 발행
     */
    private void publishEvent(TenantEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(
                    tenantExchange,
                    routingKey,
                    event
            );
            log.debug("Event published successfully: routingKey={}, event={}", routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish event: routingKey={}, event={}, error={}", 
                    routingKey, event, e.getMessage(), e);
            throw new RuntimeException("Failed to publish tenant event", e);
        }
    }
}