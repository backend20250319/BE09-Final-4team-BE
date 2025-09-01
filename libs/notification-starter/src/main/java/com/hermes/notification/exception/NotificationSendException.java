package com.hermes.notification.exception;

import com.hermes.notification.enums.NotificationType;
import lombok.Getter;

@Getter
public class NotificationSendException extends RuntimeException {

  private final Long userId;
  private final NotificationType type;

  public NotificationSendException(Long userId, NotificationType type, String message,
      Throwable cause) {
    super(message, cause);
    this.userId = userId;
    this.type = type;
  }

  public NotificationSendException(Long userId, NotificationType type, Throwable cause) {
    super(cause);
    this.userId = userId;
    this.type = type;
  }

}