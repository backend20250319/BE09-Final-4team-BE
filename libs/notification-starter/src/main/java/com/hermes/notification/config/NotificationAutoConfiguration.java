package com.hermes.notification.config;

import com.hermes.notification.publisher.NotificationPublisher;
import com.hermes.notification.sender.NotificationSender;
import com.hermes.notification.sender.RabbitNotificationSender;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableRabbit
@ComponentScan(basePackages = "com.hermes.notification")
@ConditionalOnProperty(name = "hermes.notification.enabled", havingValue = "true")
public class NotificationAutoConfiguration {

  @Value("${hermes.notification.exchange:notification.exchange}")
  private String exchangeName;

  @Value("${hermes.notification.queue:notification.create}")
  private String queueName;

  @Value("${hermes.notification.routing-key:notification.create}")
  private String routingKey;

  @Bean
  @ConditionalOnMissingBean // 교환기로 TopicExchange 사용
  public TopicExchange notificationExchange() {
    return new TopicExchange(exchangeName);
  }

  @Bean
  @ConditionalOnMissingBean
  public Queue notificationQueue() {
    return QueueBuilder.durable(queueName).build();
  }

  @Bean
  @ConditionalOnMissingBean
  public Binding notificationBinding() {
    return BindingBuilder.bind(notificationQueue())
        .to(notificationExchange())
        .with(routingKey);
  }

  @Bean
  @ConditionalOnMissingBean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(new Jackson2JsonMessageConverter());
    return template;
  }


  @Bean
  @ConditionalOnMissingBean
  public NotificationSender notificationSender(RabbitTemplate rabbitTemplate) {
    return new RabbitNotificationSender(rabbitTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(new Jackson2JsonMessageConverter());
    return factory;
  }

  @Bean
  @ConditionalOnMissingBean
  public NotificationPublisher notificationPublisher(NotificationSender notificationSender) {
    return new NotificationPublisher(notificationSender);
  }

  @Bean
  @ConditionalOnMissingBean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

}