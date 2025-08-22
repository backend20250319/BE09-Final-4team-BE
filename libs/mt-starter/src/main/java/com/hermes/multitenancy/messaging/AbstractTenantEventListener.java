package com.hermes.multitenancy.messaging;

import com.hermes.multitenancy.context.TenantContext;
import com.hermes.multitenancy.dto.TenantInfo;
import com.hermes.events.tenant.TenantEvent;
import com.hermes.multitenancy.config.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 테넌트 이벤트를 처리하는 추상 리스너 클래스
 * 각 서비스에서 이 클래스를 상속받아 구체적인 처리 로직을 구현
 */
@Slf4j
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "hermes.multitenancy.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public abstract class AbstractTenantEventListener {

    private final RabbitMQProperties properties;

    /**
     * 서비스명을 반환 (각 서비스에서 구현 필요)
     */
    protected abstract String getServiceName();

    /**
     * 테넌트 생성 시 처리할 로직 (각 서비스에서 구현)
     */
    protected abstract void handleTenantCreated(TenantEvent event);

    /**
     * 테넌트 삭제 시 처리할 로직 (각 서비스에서 구현)
     */
    protected abstract void handleTenantDeleted(TenantEvent event);

    /**
     * 테넌트 업데이트 시 처리할 로직 (각 서비스에서 구현, 기본적으로 로그만 출력)
     */
    protected void handleTenantUpdated(TenantEvent event) {
        log.info("[{}] Tenant UPDATED event received (no action needed): tenantId={}, schemaName={}", 
                getServiceName(), event.getTenantId(), event.getSchemaName());
    }

    /**
     * 테넌트 상태 변경 시 처리할 로직 (각 서비스에서 구현, 기본적으로 로그만 출력)
     */
    protected void handleTenantStatusChanged(TenantEvent event) {
        log.info("[{}] Tenant STATUS_CHANGED event received (no action needed): tenantId={}, schemaName={}", 
                getServiceName(), event.getTenantId(), event.getSchemaName());
    }

    /**
     * RabbitMQ 메시지 핸들러
     * 실제 Queue 이름은 각 서비스의 Configuration에서 정의됨
     */
    @RabbitHandler
    public void handleTenantEvent(TenantEvent event) {
        String serviceName = getServiceName();
        log.info("[{}] Tenant Event Received: Type={}, TenantId={}, SchemaName={}", 
                serviceName, event.getEventType(), event.getTenantId(), event.getSchemaName());

        // 시스템 테넌트 컨텍스트에서 스키마 작업 수행
        TenantContext.executeWithTenant(getSystemTenantInfo(), () -> {
            try {
                switch (event.getEventType()) {
                    case TENANT_CREATED:
                        handleTenantCreated(event);
                        break;
                    case TENANT_DELETED:
                        handleTenantDeleted(event);
                        break;
                    case TENANT_UPDATED:
                        handleTenantUpdated(event);
                        break;
                    case TENANT_STATUS_CHANGED:
                        handleTenantStatusChanged(event);
                        break;
                    default:
                        log.warn("[{}] Unhandled Tenant Event Type: {}", serviceName, event.getEventType());
                }
            } catch (Exception e) {
                log.error("[{}] Error processing tenant event for tenantId {}: {}", 
                        serviceName, event.getTenantId(), e.getMessage(), e);
                
                // 예외를 다시 던져서 RabbitMQ가 재시도하거나 DLQ로 보낼 수 있도록 함
                throw new RuntimeException("Failed to process tenant event in " + serviceName, e);
            }
            return null;
        });
    }

    /**
     * 시스템 테넌트 정보 반환
     * 기본적으로 public 스키마를 사용하지만, 각 서비스에서 오버라이드 가능
     */
    protected TenantInfo getSystemTenantInfo() {
        return new TenantInfo("system", "public");
    }

    /**
     * 재시도 관련 설정 정보 반환
     */
    protected int getMaxRetryCount() {
        return properties.getMaxRetryCount();
    }

    protected long getRetryDelay() {
        return properties.getRetryDelay();
    }
}
