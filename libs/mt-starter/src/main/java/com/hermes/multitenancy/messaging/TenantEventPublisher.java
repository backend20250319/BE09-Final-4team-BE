package com.hermes.multitenancy.messaging;

import com.hermes.multitenancy.config.RabbitMQProperties;
import com.hermes.multitenancy.event.TenantEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 테넌트 이벤트를 RabbitMQ로 발행하는 Publisher
 */
@Slf4j
@RequiredArgsConstructor
public class TenantEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    /**
     * 테넌트 생성 이벤트 발행
     */
    public void publishTenantCreated(String tenantId, String tenantName, String schemaName, String adminEmail) {
        TenantEvent event = TenantEvent.created(tenantId, tenantName, schemaName, adminEmail);
        
        publishEvent(event, properties.getTenantCreatedRoutingKey());
        log.info("Tenant CREATED event published: tenantId={}, schemaName={}", tenantId, schemaName);
    }

    /**
     * 테넌트 삭제 이벤트 발행
     */
    public void publishTenantDeleted(String tenantId, String schemaName) {
        TenantEvent event = TenantEvent.deleted(tenantId, schemaName);
        
        publishEvent(event, properties.getTenantDeletedRoutingKey());
        log.info("Tenant DELETED event published: tenantId={}, schemaName={}", tenantId, schemaName);
    }

    /**
     * 테넌트 업데이트 이벤트 발행
     */
    public void publishTenantUpdated(String tenantId, String tenantName, String schemaName, String adminEmail) {
        TenantEvent event = new TenantEvent(
                TenantEvent.EventType.TENANT_UPDATED,
                tenantId,
                tenantName,
                schemaName,
                "ACTIVE",
                adminEmail,
                java.time.LocalDateTime.now()
        );
        
        publishEvent(event, properties.getTenantUpdatedRoutingKey());
        log.info("Tenant UPDATED event published: tenantId={}, schemaName={}", tenantId, schemaName);
    }

    /**
     * 테넌트 상태 변경 이벤트 발행
     */
    public void publishTenantStatusChanged(String tenantId, String schemaName, String status) {
        TenantEvent event = new TenantEvent(
                TenantEvent.EventType.TENANT_STATUS_CHANGED,
                tenantId,
                null,
                schemaName,
                status,
                null,
                java.time.LocalDateTime.now()
        );
        
        publishEvent(event, properties.getTenantStatusChangedRoutingKey());
        log.info("Tenant STATUS_CHANGED event published: tenantId={}, schemaName={}, status={}", 
                tenantId, schemaName, status);
    }

    /**
     * 이벤트를 RabbitMQ로 발행
     */
    private void publishEvent(TenantEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(
                    properties.getTenantExchange(),
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
