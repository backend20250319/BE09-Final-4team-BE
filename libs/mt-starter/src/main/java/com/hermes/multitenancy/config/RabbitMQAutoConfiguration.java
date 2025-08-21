package com.hermes.multitenancy.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 자동 구성
 */
@Slf4j
@Configuration
@ConditionalOnClass({RabbitTemplate.class, ConnectionFactory.class})
@ConditionalOnProperty(prefix = "hermes.multitenancy.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RabbitMQProperties.class)
@RequiredArgsConstructor
public class RabbitMQAutoConfiguration {

    private final RabbitMQProperties properties;

    /**
     * JSON 메시지 컨버터
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 설정
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 테넌트 이벤트 Exchange (Topic Exchange)
     */
    @Bean
    public TopicExchange tenantEventExchange() {
        return ExchangeBuilder
                .topicExchange(properties.getTenantExchange())
                .durable(true)
                .build();
    }

    /**
     * Dead Letter Exchange
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(properties.getDeadLetterExchange())
                .durable(true)
                .build();
    }

    /**
     * 서비스별 테넌트 이벤트 Queue 생성 팩토리 메서드
     * 각 서비스에서 자신의 서비스명을 사용하여 Queue를 생성할 수 있도록 제공
     */
    public Queue createTenantEventQueue(String serviceName) {
        String queueName = properties.getTenantQueuePattern()
                .replace("{serviceName}", serviceName);
        
        return QueueBuilder
                .durable(queueName)
                .withArgument("x-dead-letter-exchange", properties.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", "dlq." + serviceName)
                .build();
    }

    /**
     * 서비스별 Dead Letter Queue 생성 팩토리 메서드
     */
    public Queue createDeadLetterQueue(String serviceName) {
        String queueName = properties.getDeadLetterQueuePattern()
                .replace("{serviceName}", serviceName);
        
        return QueueBuilder
                .durable(queueName)
                .build();
    }

    /**
     * 테넌트 이벤트 Queue와 Exchange 바인딩 생성 팩토리 메서드
     */
    public Binding createTenantEventBinding(Queue queue, TopicExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("tenant.*"); // 모든 테넌트 이벤트를 받음
    }

    /**
     * Dead Letter Queue와 Dead Letter Exchange 바인딩 생성 팩토리 메서드
     */
    public Binding createDeadLetterBinding(Queue dlq, DirectExchange dlx, String serviceName) {
        return BindingBuilder
                .bind(dlq)
                .to(dlx)
                .with("dlq." + serviceName);
    }

    /**
     * RabbitMQ 설정 정보 로깅
     */
    @Bean
    public Object logRabbitMQConfiguration() {
        log.info("RabbitMQ Configuration:");
        log.info("  - Tenant Exchange: {}", properties.getTenantExchange());
        log.info("  - Queue Pattern: {}", properties.getTenantQueuePattern());
        log.info("  - Dead Letter Exchange: {}", properties.getDeadLetterExchange());
        log.info("  - Max Retry Count: {}", properties.getMaxRetryCount());
        log.info("  - Retry Delay: {}ms", properties.getRetryDelay());
        return new Object();
    }
}
