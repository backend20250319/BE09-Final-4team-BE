package com.hermes.communicationservice.notification.listener;

import com.hermes.communicationservice.notification.dto.CreateNotificationRequestDto;
import com.hermes.communicationservice.notification.dto.NotificationResponseDto;
import com.hermes.communicationservice.notification.service.NotificationService;
import com.hermes.communicationservice.websocket.service.WebSocketNotificationService;
import com.hermes.notification.event.NotificationEvent;
import com.hermes.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;
    
    // 이미 브로드캐스트된 공지사항 ID를 추적하기 위한 Set (중복 방지)
    private final Set<Long> broadcastedAnnouncements = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = "notification.create")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("알림 이벤트 수신: userId={}, type={}, content={}", 
                event.getUserId(), event.getType(), event.getContent());

        try {
            // 1. 기존 DB 저장 로직
            CreateNotificationRequestDto requestDto = new CreateNotificationRequestDto(
                    event.getUserId(),
                    event.getType(),
                    event.getContent(),
                    event.getReferenceId(),
                    event.getCreatedAt()
            );

            NotificationResponseDto savedNotification = notificationService.createNotification(requestDto);
            log.info("알림 DB 저장 완료: notificationId={}, userId={}, type={}", 
                    savedNotification.getId(), event.getUserId(), event.getType());
            
            // 2. 실시간 WebSocket 알림 전송 (저장된 알림 ID 포함)
            sendRealtimeNotification(event, savedNotification.getId());
            
        } catch (Exception e) {
            log.error("알림 처리 실패: userId={}, type={}, error={}", 
                    event.getUserId(), event.getType(), e.getMessage(), e);
            // 필요시 DLQ(Dead Letter Queue) 처리나 재시도 로직 추가
        }
    }

    /**
     * 알림 타입에 따라 적절한 실시간 알림을 전송합니다.
     */
    private void sendRealtimeNotification(NotificationEvent event, Long notificationId) {
        try {
            // 공지사항의 경우 전체 브로드캐스트 (중복 방지)
            if (event.getType() == NotificationType.ANNOUNCEMENT) {
                Long referenceId = event.getReferenceId();
                
                // 이미 브로드캐스트된 공지사항인지 확인
                if (broadcastedAnnouncements.add(referenceId)) {
                    log.info("공지사항 브로드캐스트 알림 전송: notificationId={}, referenceId={}, type={}", 
                            notificationId, referenceId, event.getType());
                    webSocketNotificationService.sendBroadcastNotification(event, notificationId);
                } else {
                    log.debug("이미 브로드캐스트된 공지사항 스킵: referenceId={}, userId={}", 
                            referenceId, event.getUserId());
                }
            } 
            // 개인 알림 (결재 관련 등)
            else {
                log.info("개인 실시간 알림 전송: notificationId={}, userId={}, type={}", 
                        notificationId, event.getUserId(), event.getType());
                webSocketNotificationService.sendNotificationToUser(event, notificationId);
            }
            
        } catch (Exception e) {
            log.error("실시간 알림 전송 실패: userId={}, type={}, error={}", 
                    event.getUserId(), event.getType(), e.getMessage(), e);
            // 실시간 알림 실패해도 DB 저장은 성공했으므로 전체 트랜잭션은 실패하지 않음
        }
    }
}