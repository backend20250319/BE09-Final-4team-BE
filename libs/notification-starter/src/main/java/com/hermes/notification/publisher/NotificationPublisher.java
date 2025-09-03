package com.hermes.notification.publisher;

import com.hermes.notification.dto.NotificationRequest;
import com.hermes.notification.dto.NotificationResponse;
import com.hermes.notification.event.NotificationEvent;
import com.hermes.notification.exception.NotificationPublishException;
import com.hermes.notification.exception.NotificationSendException;
import com.hermes.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

  private final NotificationSender notificationSender;

  public NotificationResponse publish(NotificationRequest request) {
    String typeDescription = "[" + request.getType() + "]";
    log.info("{} 알림 발송 시작: content={}, 대상자 수={}",
        typeDescription, request.getContent(), request.getUserIds().size());

    List<NotificationSendException> failures = new ArrayList<>();
    AtomicInteger successCount = new AtomicInteger(0);

    request.getUserIds().forEach(userId -> {
      try {
        NotificationEvent event = NotificationEvent.builder()
            .userId(userId)
            .type(request.getType())
            .content(request.getContent())
            .referenceId(request.getReferenceId())
            .createdAt(request.getCreatedAt())
            .build();

        notificationSender.sendNotification(event);
        successCount.incrementAndGet();

      } catch (Exception e) {
        NotificationSendException sendException = new NotificationSendException(
            userId, request.getType(), e);
        failures.add(sendException);
        log.warn("개별 알림 발송 실패: userId={}, type={}, error={}",
            userId, request.getType(), e.getMessage());
      }
    });

    NotificationResponse result = new NotificationResponse(
        request.getType(), successCount.get(), request.getUserIds().size(), failures);

    // 결과 로깅 및 처리
    if (result.isCompleteSuccess()) {
      log.info("{} 알림 발송 완전 성공: content={}, 성공 {}명",
          typeDescription, request.getContent(), result.getSuccessCount());
    } else if (result.isCompleteFailure()) {
      log.error("{} 알림 발송 완전 실패: content={}, 총 {}명 모두 실패",
          typeDescription, request.getContent(), result.getTotalCount());
      logFailedUsers(failures, typeDescription);

      // 완전 실패 시 예외 발생
      throw new NotificationPublishException(
          request.getType(), failures, result.getSuccessCount(), result.getTotalCount());
    } else {
      log.warn("{} 알림 발송 부분 실패: content={}, 성공 {}/{}, 실패 {}명",
          typeDescription, request.getContent(), result.getSuccessCount(),
          result.getTotalCount(), result.getFailureCount());
      logFailedUsers(failures, typeDescription);
    }

    return result;
  }

  private void logFailedUsers(List<NotificationSendException> failures, String typeDescription) {
    log.error("{} 알림 발송 실패 사용자 목록:", typeDescription);
    failures.forEach(failure ->
        log.error("  - userId: {}, 원인: {}", failure.getUserId(), failure.getCause().getMessage()));
  }


}