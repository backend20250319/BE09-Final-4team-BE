package com.hermes.notification.sender;

import com.hermes.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitNotificationSender implements NotificationSender {

  private final RabbitTemplate rabbitTemplate;

  @Value("${hermes.notification.exchange:notification.exchange}")
  private String exchange;

  @Value("${hermes.notification.routing-key:notification.create}")
  private String routingKey;

  @Override
  public void sendNotification(NotificationEvent event) {
    try {
      rabbitTemplate.convertAndSend(exchange, routingKey, event);
      log.debug("알림 이벤트 발송 완료: userId={}, type={}, content={}",
          event.getUserId(), event.getType(), event.getContent());
    } catch (Exception e) {
      log.error("알림 이벤트 발송 실패: userId={}, type={}, error={}",
          event.getUserId(), event.getType(), e.getMessage(), e);
      throw e;
    }
  }

}