package com.hermes.multitenancy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RabbitMQ 설정 속성
 */
@Data
@ConfigurationProperties(prefix = "hermes.multitenancy.rabbitmq")
public class RabbitMQProperties {
    
    /**
     * RabbitMQ 사용 여부
     */
    private boolean enabled = true;
    
    /**
     * 테넌트 이벤트 Exchange 이름
     */
    private String tenantExchange = "tenant.events";
    
    /**
     * 테넌트 이벤트 Queue 이름 패턴 ({serviceName}이 실제 서비스명으로 치환됨)
     */
    private String tenantQueuePattern = "tenant.events.{serviceName}";
    
    /**
     * 테넌트 생성 이벤트 라우팅 키
     */
    private String tenantCreatedRoutingKey = "tenant.created";
    
    /**
     * 테넌트 삭제 이벤트 라우팅 키  
     */
    private String tenantDeletedRoutingKey = "tenant.deleted";
    
    /**
     * 테넌트 업데이트 이벤트 라우팅 키
     */
    private String tenantUpdatedRoutingKey = "tenant.updated";
    
    /**
     * 테넌트 상태 변경 이벤트 라우팅 키
     */
    private String tenantStatusChangedRoutingKey = "tenant.status.changed";
    
    /**
     * Dead Letter Exchange 이름
     */
    private String deadLetterExchange = "tenant.events.dlx";
    
    /**
     * Dead Letter Queue 이름 패턴
     */
    private String deadLetterQueuePattern = "tenant.events.dlq.{serviceName}";
    
    /**
     * 메시지 재시도 횟수
     */
    private int maxRetryCount = 3;
    
    /**
     * 재시도 간격 (밀리초)
     */
    private long retryDelay = 5000L;
}
