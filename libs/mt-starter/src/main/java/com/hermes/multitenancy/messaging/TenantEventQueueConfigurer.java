package com.hermes.multitenancy.messaging;

import com.hermes.multitenancy.config.RabbitMQAutoConfiguration;
import com.hermes.multitenancy.config.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 서비스별 테넌트 이벤트 Queue 설정을 도와주는 클래스
 * 각 서비스에서 이 클래스를 사용하여 자신만의 Queue와 Binding을 생성
 */
@Slf4j
@Component
@ConditionalOnClass({RabbitTemplate.class, Queue.class})
@ConditionalOnProperty(prefix = "hermes.multitenancy.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class TenantEventQueueConfigurer {

    private final RabbitMQAutoConfiguration rabbitMQAutoConfiguration;
    private final RabbitMQProperties properties;

    /**
     * 지정된 서비스명에 대한 테넌트 이벤트 Queue 생성
     */
    public Queue createTenantEventQueue(String serviceName) {
        Queue queue = rabbitMQAutoConfiguration.createTenantEventQueue(serviceName);
        log.info("Created tenant event queue for service '{}': {}", serviceName, queue.getName());
        return queue;
    }

    /**
     * 지정된 서비스명에 대한 Dead Letter Queue 생성
     */
    public Queue createDeadLetterQueue(String serviceName) {
        Queue dlq = rabbitMQAutoConfiguration.createDeadLetterQueue(serviceName);
        log.info("Created dead letter queue for service '{}': {}", serviceName, dlq.getName());
        return dlq;
    }

    /**
     * 테넌트 이벤트 Queue를 Exchange에 바인딩
     */
    public Binding createTenantEventBinding(Queue queue, TopicExchange exchange) {
        Binding binding = rabbitMQAutoConfiguration.createTenantEventBinding(queue, exchange);
        log.info("Created tenant event binding: {} -> {} with pattern 'tenant.*'", 
                exchange.getName(), queue.getName());
        return binding;
    }

    /**
     * Dead Letter Queue를 Dead Letter Exchange에 바인딩
     */
    public Binding createDeadLetterBinding(Queue dlq, DirectExchange dlx, String serviceName) {
        Binding binding = rabbitMQAutoConfiguration.createDeadLetterBinding(dlq, dlx, serviceName);
        log.info("Created dead letter binding: {} -> {} with routing key 'dlq.{}'", 
                dlx.getName(), dlq.getName(), serviceName);
        return binding;
    }

    /**
     * 서비스에 필요한 모든 Queue와 Binding을 한번에 생성하는 편의 메서드
     */
    public QueueConfiguration createCompleteConfiguration(String serviceName, 
                                                         TopicExchange tenantExchange,
                                                         DirectExchange deadLetterExchange) {
        // 1. 메인 Queue 생성
        Queue tenantQueue = createTenantEventQueue(serviceName);
        
        // 2. Dead Letter Queue 생성
        Queue deadLetterQueue = createDeadLetterQueue(serviceName);
        
        // 3. 메인 바인딩 생성
        Binding tenantBinding = createTenantEventBinding(tenantQueue, tenantExchange);
        
        // 4. Dead Letter 바인딩 생성
        Binding deadLetterBinding = createDeadLetterBinding(deadLetterQueue, deadLetterExchange, serviceName);
        
        log.info("Complete RabbitMQ configuration created for service '{}'", serviceName);
        
        return new QueueConfiguration(tenantQueue, deadLetterQueue, tenantBinding, deadLetterBinding);
    }

    /**
     * 생성된 Queue와 Binding 정보를 담는 결과 클래스
     */
    public static class QueueConfiguration {
        public final Queue tenantQueue;
        public final Queue deadLetterQueue;
        public final Binding tenantBinding;
        public final Binding deadLetterBinding;

        public QueueConfiguration(Queue tenantQueue, Queue deadLetterQueue, 
                                Binding tenantBinding, Binding deadLetterBinding) {
            this.tenantQueue = tenantQueue;
            this.deadLetterQueue = deadLetterQueue;
            this.tenantBinding = tenantBinding;
            this.deadLetterBinding = deadLetterBinding;
        }
    }
}
