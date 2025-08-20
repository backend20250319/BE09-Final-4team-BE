package com.hermes.multitenancy.config;

import com.hermes.multitenancy.messaging.DefaultTenantEventListener;
import com.hermes.multitenancy.messaging.TenantEventPublisher;
import com.hermes.multitenancy.util.SchemaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 테넌트 이벤트 처리를 위한 완전 자동 구성
 * 각 서비스는 의존성만 추가하면 자동으로 멀티테넌시 이벤트 처리가 활성화됩니다.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "hermes.multitenancy.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RabbitMQProperties.class)
public class TenantEventAutoConfiguration {

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    /**
     * 현재 서비스를 위한 테넌트 이벤트 Queue 자동 생성
     */
    @Bean
    public Queue tenantEventQueue(RabbitMQProperties properties) {
        String queueName = properties.getTenantQueuePattern()
                .replace("{serviceName}", serviceName);
        
        Queue queue = QueueBuilder
                .durable(queueName)
                .withArgument("x-dead-letter-exchange", properties.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", "dlq." + serviceName)
                .build();
        
        log.info("Auto-created tenant event queue for service '{}': {}", serviceName, queueName);
        return queue;
    }

    /**
     * 현재 서비스를 위한 Dead Letter Queue 자동 생성
     */
    @Bean
    public Queue tenantEventDeadLetterQueue(RabbitMQProperties properties) {
        String queueName = properties.getDeadLetterQueuePattern()
                .replace("{serviceName}", serviceName);
        
        Queue dlq = QueueBuilder
                .durable(queueName)
                .build();
        
        log.info("Auto-created dead letter queue for service '{}': {}", serviceName, queueName);
        return dlq;
    }

    /**
     * 테넌트 이벤트 Exchange (Topic Exchange) - Auto Configuration용
     */
    @Bean
    @ConditionalOnMissingBean(name = "tenantEventExchange")
    public TopicExchange tenantEventExchange(RabbitMQProperties properties) {
        log.info("Auto-creating tenant event exchange: {}", properties.getTenantExchange());
        return ExchangeBuilder
                .topicExchange(properties.getTenantExchange())
                .durable(true)
                .build();
    }

    /**
     * Dead Letter Exchange - Auto Configuration용
     */
    @Bean
    @ConditionalOnMissingBean(name = "deadLetterExchange")
    public DirectExchange deadLetterExchange(RabbitMQProperties properties) {
        log.info("Auto-creating dead letter exchange: {}", properties.getDeadLetterExchange());
        return ExchangeBuilder
                .directExchange(properties.getDeadLetterExchange())
                .durable(true)
                .build();
    }

    /**
     * JSON 메시지 컨버터 - Auto Configuration용
     */
    @Bean
    @ConditionalOnMissingBean(Jackson2JsonMessageConverter.class)
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        log.info("Auto-creating Jackson2JsonMessageConverter");
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate - Auto Configuration용
     */
    @Bean
    @ConditionalOnMissingBean(RabbitTemplate.class)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                       Jackson2JsonMessageConverter messageConverter) {
        log.info("Auto-creating RabbitTemplate");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    /**
     * 테넌트 이벤트 Queue와 Exchange 바인딩 자동 생성
     */
    @Bean
    public Binding tenantEventBinding(
            @Qualifier("tenantEventQueue") Queue tenantEventQueue, 
            @Qualifier("tenantEventExchange") TopicExchange tenantEventExchange) {
        Binding binding = BindingBuilder
                .bind(tenantEventQueue)
                .to(tenantEventExchange)
                .with("tenant.*"); // 모든 테넌트 이벤트를 받음
        
        log.info("Auto-created tenant event binding: {} -> {} with pattern 'tenant.*'", 
                tenantEventExchange.getName(), tenantEventQueue.getName());
        return binding;
    }

    /**
     * Dead Letter Queue와 Dead Letter Exchange 바인딩 자동 생성
     */
    @Bean
    public Binding tenantEventDeadLetterBinding(
            @Qualifier("tenantEventDeadLetterQueue") Queue tenantEventDeadLetterQueue, 
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        Binding binding = BindingBuilder
                .bind(tenantEventDeadLetterQueue)
                .to(deadLetterExchange)
                .with("dlq." + serviceName);
        
        log.info("Auto-created dead letter binding: {} -> {} with routing key 'dlq.{}'", 
                deadLetterExchange.getName(), tenantEventDeadLetterQueue.getName(), serviceName);
        return binding;
    }

    /**
     * 기본 테넌트 이벤트 리스너 자동 등록
     * 사용자가 별도의 리스너를 구현하지 않은 경우에만 등록됩니다.
     * 멀티테넌시가 활성화된 경우에만 등록됩니다 (SchemaUtils가 필요하므로).
     */
    @Bean
    @ConditionalOnMissingBean(name = "tenantEventListener")
    @ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true")
    public DefaultTenantEventListener defaultTenantEventListener(RabbitMQProperties properties, SchemaUtils schemaUtils) {
        log.info("Auto-registering default tenant event listener for service '{}'", serviceName);
        return new DefaultTenantEventListener(properties, schemaUtils, serviceName);
    }

    /**
     * TenantEventPublisher 빈 자동 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantEventPublisher tenantEventPublisher(RabbitTemplate rabbitTemplate, RabbitMQProperties properties) {
        log.info("Auto-registering TenantEventPublisher for service '{}'", serviceName);
        return new TenantEventPublisher(rabbitTemplate, properties);
    }

    /**
     * 자동 구성 정보 로깅
     */
    @Bean
    public Object logTenantEventAutoConfiguration() {
        log.info("Tenant Event Auto Configuration for service '{}' completed:", serviceName);
        log.info("  - Queue: tenant.events.{}", serviceName);
        log.info("  - Dead Letter Queue: tenant.events.dlq.{}", serviceName);
        log.info("  - TenantEventPublisher: Auto-registered");
        log.info("  - Default event listener: {}", "Auto-registered (if no custom listener)");
        return new Object();
    }
}
