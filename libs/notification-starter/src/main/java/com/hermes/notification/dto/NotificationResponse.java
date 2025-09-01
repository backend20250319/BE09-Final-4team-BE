package com.hermes.notification.dto;

import com.hermes.notification.enums.NotificationType;
import com.hermes.notification.exception.NotificationSendException;
import lombok.Getter;

import java.util.List;

@Getter
public class NotificationResponse {

  private final NotificationType type;
  private final int successCount;
  private final int totalCount;
  private final List<NotificationSendException> failures;

  public NotificationResponse(NotificationType type, int successCount, int totalCount,
      List<NotificationSendException> failures) {
    this.type = type;
    this.successCount = successCount;
    this.totalCount = totalCount;
    this.failures = failures;
  }

  public boolean hasFailures() {
    return !failures.isEmpty();
  }

  public boolean isCompleteSuccess() {
    return failures.isEmpty();
  }

  public boolean isCompleteFailure() {
    return successCount == 0;
  }

  public int getFailureCount() {
    return failures.size();
  }

  public String getSummary() {
    return String.format("[%s] 알림 발송 완료: 성공 %d/%d, 실패 %d건",
        type, successCount, totalCount, getFailureCount());
  }

}