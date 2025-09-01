package com.hermes.communicationservice.notification.dto;

import com.hermes.communicationservice.notification.entity.Notification;
import com.hermes.notification.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateNotificationRequestDto {

  @NotNull(message = "수신자 ID는 필수입니다")
  private Long userId;

  @NotNull(message = "알림 타입은 필수입니다")
  private NotificationType type;

  private String content;

  private Long referenceId;

  @NotNull(message = "알림 발행 날짜는 필수입니다")
  private LocalDateTime createdAt;

  public Notification toEntity() {
    return Notification.builder()
        .userId(this.userId)
        .type(this.type)
        .content(this.content)
        .referenceId(this.referenceId)
        .createdAt(createdAt)
        .build();
  }

}