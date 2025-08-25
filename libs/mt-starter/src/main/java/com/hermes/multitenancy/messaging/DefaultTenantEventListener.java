package com.hermes.multitenancy.messaging;

import com.hermes.multitenancy.config.RabbitMQProperties;
import com.hermes.events.tenant.TenantEvent;
import com.hermes.multitenancy.util.SchemaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기본 테넌트 이벤트 리스너 구현체
 * 대부분의 서비스에서 필요한 기본적인 스키마 생성/삭제 로직을 제공합니다.
 * 
 * 사용자 정의 리스너가 없는 경우 자동으로 등록되며,
 * 사용자가 별도의 리스너를 구현한 경우에는 그것을 우선 사용합니다.
 */
@Slf4j
@ConditionalOnClass(org.springframework.amqp.rabbit.core.RabbitTemplate.class)
@ConditionalOnProperty(prefix = "hermes.multitenancy.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DefaultTenantEventListener extends AbstractTenantEventListener {

    private final SchemaUtils schemaUtils;
    private final String serviceName;

    public DefaultTenantEventListener(RabbitMQProperties properties, SchemaUtils schemaUtils, String serviceName) {
        super(properties);
        this.schemaUtils = schemaUtils;
        this.serviceName = serviceName;
        log.info("Default Tenant Event Listener initialized for service '{}'", serviceName);
    }

    @Override
    protected String getServiceName() {
        return serviceName;
    }

    /**
     * RabbitMQ 메시지 리스너 - 동적 큐 이름 사용
     * SpEL을 사용하여 서비스명에 따라 동적으로 큐 이름을 결정합니다.
     */
    @RabbitListener(queues = "#{tenantEventQueue.name}")
    @Override
    public void handleTenantEvent(TenantEvent event) {
        super.handleTenantEvent(event); // 부모 클래스의 공통 로직 호출
    }

    @Override
    @Transactional
    protected void handleTenantCreated(TenantEvent event) {
        String schemaName = event.getSchemaName();
        String tenantId = event.getTenantId();
        
        log.info("[{}] 테넌트 생성 이벤트 처리 시작: tenantId={}, schemaName={}", 
                serviceName, tenantId, schemaName);
        
        try {
            if (!schemaUtils.schemaExists(schemaName)) {
                log.info("[{}] 새 테넌트 스키마 생성 시작: schemaName={}", serviceName, schemaName);
                schemaUtils.createSchema(schemaName);
                log.info("[{}] 새 테넌트 스키마 생성 완료: schemaName={}", serviceName, schemaName);
            } else {
                log.warn("[{}] 스키마가 이미 존재합니다. 생성 건너뛰기: schemaName={}", serviceName, schemaName);
            }
        } catch (Exception e) {
            log.error("[{}] 테넌트 스키마 생성 실패: tenantId={}, schemaName={}, error={}", 
                    serviceName, tenantId, schemaName, e.getMessage(), e);
            throw e; // 재시도를 위해 예외를 다시 던짐
        }
    }

    @Override
    @Transactional
    protected void handleTenantDeleted(TenantEvent event) {
        String schemaName = event.getSchemaName();
        String tenantId = event.getTenantId();
        
        log.warn("[{}] 테넌트 삭제 이벤트 처리 시작: tenantId={}, schemaName={}", 
                serviceName, tenantId, schemaName);
        
        try {
            if (schemaUtils.schemaExists(schemaName)) {
                log.warn("[{}] 테넌트 스키마 삭제 시작: schemaName={}", serviceName, schemaName);
                schemaUtils.dropSchema(schemaName);
                log.warn("[{}] 테넌트 스키마 삭제 완료: schemaName={}", serviceName, schemaName);
            } else {
                log.warn("[{}] 삭제할 스키마가 존재하지 않습니다. 삭제 건너뛰기: schemaName={}", 
                        serviceName, schemaName);
            }
        } catch (Exception e) {
            log.error("[{}] 테넌트 스키마 삭제 실패: tenantId={}, schemaName={}, error={}", 
                    serviceName, tenantId, schemaName, e.getMessage(), e);
            throw e; // 재시도를 위해 예외를 다시 던짐
        }
    }

    @Override
    protected void handleTenantUpdated(TenantEvent event) {
        log.info("[{}] 테넌트 업데이트 이벤트 수신 (추가 작업 없음): tenantId={}, schemaName={}", 
                serviceName, event.getTenantId(), event.getSchemaName());
        // 기본 구현: 테넌트 업데이트 시 특별한 작업이 필요없으므로 로그만 출력
        // 필요한 경우 각 서비스에서 오버라이드 가능
    }

    @Override
    protected void handleTenantStatusChanged(TenantEvent event) {
        log.info("[{}] 테넌트 상태 변경 이벤트 수신 (추가 작업 없음): tenantId={}, schemaName={}", 
                serviceName, event.getTenantId(), event.getSchemaName());
        // 기본 구현: 테넌트 상태 변경 시 특별한 작업이 필요없으므로 로그만 출력
        // 필요한 경우 각 서비스에서 오버라이드 가능
    }
}
