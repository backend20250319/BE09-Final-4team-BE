package com.hermes.communicationservice.notification.listener;

import com.hermes.communicationservice.notification.dto.CreateNotificationRequestDto;
import com.hermes.communicationservice.notification.service.NotificationService;
import com.hermes.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "notification.create")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("알림 이벤트 수신: userId={}, type={}, content={}", 
                event.getUserId(), event.getType(), event.getContent());

        try {
            CreateNotificationRequestDto requestDto = new CreateNotificationRequestDto(
                    event.getUserId(),
                    event.getType(),
                    event.getContent(),
                    event.getReferenceId(),
                    event.getCreatedAt()
            );

            notificationService.createNotification(requestDto);
            
            log.info("알림 생성 완료: userId={}, type={}", event.getUserId(), event.getType());
        } catch (Exception e) {
            log.error("알림 생성 실패: userId={}, type={}, error={}", 
                    event.getUserId(), event.getType(), e.getMessage(), e);
            // 필요시 DLQ(Dead Letter Queue) 처리나 재시도 로직 추가
        }
    }
}