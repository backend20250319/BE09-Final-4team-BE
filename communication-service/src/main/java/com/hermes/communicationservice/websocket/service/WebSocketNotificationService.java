package com.hermes.communicationservice.websocket.service;

import com.hermes.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

/**
 * WebSocket을 통한 실시간 알림 전송을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

  private final SimpMessagingTemplate messagingTemplate;

  /**
   * 특정 사용자에게 실시간 알림을 전송합니다.
   *
   * @param event          알림 이벤트 정보
   * @param notificationId 저장된 알림의 PK
   */
  public void sendNotificationToUser(NotificationEvent event, Long notificationId) {
    try {
      String destination = "/user/" + event.getUserId() + "/queue/notifications";

      // 클라이언트에게 전송할 알림 데이터 생성
      RealtimeNotificationDto notificationDto = RealtimeNotificationDto.builder()
          .notificationId(notificationId)
          .userId(event.getUserId())
          .type(event.getType().name())
          .content(event.getContent())
          .referenceId(event.getReferenceId())
          .createdAt(event.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
          .build();

      // WebSocket을 통해 실시간 알림 전송
      messagingTemplate.convertAndSend(destination, notificationDto);

      log.info("실시간 알림 전송 성공 - userId: {}, type: {}, destination: {}",
          event.getUserId(), event.getType(), destination);

    } catch (Exception e) {
      log.error("실시간 알림 전송 실패 - userId: {}, type: {}, error: {}",
          event.getUserId(), event.getType(), e.getMessage(), e);
    }
  }

  /**
   * 전체 사용자에게 브로드캐스트 알림을 전송합니다.
   *
   * @param event          알림 이벤트 정보
   * @param notificationId 저장된 알림의 PK
   */
  public void sendBroadcastNotification(NotificationEvent event, Long notificationId) {
    try {
      String destination = "/topic/announcements";

      // 브로드캐스트 알림 데이터 생성 (공지사항은 userId 없음)
      RealtimeNotificationDto notificationDto = RealtimeNotificationDto.builder()
          .notificationId(notificationId)
          .userId(null)  // 공지사항은 전체 대상이므로 userId 없음
          .type(event.getType().name())
          .content(event.getContent())
          .referenceId(event.getReferenceId())
          .createdAt(event.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
          .build();

      // 전체 구독자에게 브로드캐스트
      messagingTemplate.convertAndSend(destination, notificationDto);

      log.info("브로드캐스트 알림 전송 성공 - type: {}, destination: {}",
          event.getType(), destination);

    } catch (Exception e) {
      log.error("브로드캐스트 알림 전송 실패 - type: {}, error: {}",
          event.getType(), e.getMessage(), e);
    }
  }

}