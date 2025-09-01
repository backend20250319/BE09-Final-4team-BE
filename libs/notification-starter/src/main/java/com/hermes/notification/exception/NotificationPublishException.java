package com.hermes.notification.exception;

import com.hermes.notification.enums.NotificationType;
import lombok.Getter;

import java.util.List;

@Getter
public class NotificationPublishException extends RuntimeException {

  private final NotificationType type;
  private final List<NotificationSendException> failedNotifications;
  private final int successCount;
  private final int totalCount;

  public NotificationPublishException(NotificationType type,
      List<NotificationSendException> failedNotifications, int successCount, int totalCount) {
    super(String.format("[%s] 알림 발송 완전 실패: 총 %d명 모두 실패",
        type, totalCount));
    this.type = type;
    this.failedNotifications = failedNotifications;
    this.successCount = successCount;
    this.totalCount = totalCount;
  }

}