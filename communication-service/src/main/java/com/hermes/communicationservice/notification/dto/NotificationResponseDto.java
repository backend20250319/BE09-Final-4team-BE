package com.hermes.communicationservice.notification.dto;

import com.hermes.communicationservice.notification.entity.Notification;
import com.hermes.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NotificationResponseDto {

  private Long id;
  private Long userId;
  private NotificationType type;
  private String content;
  private Long referenceId;
  private boolean isRead;
  private LocalDateTime createdAt;

  public static NotificationResponseDto fromEntity(Notification notification) {
    return NotificationResponseDto.builder()
        .id(notification.getId())
        .userId(notification.getUserId())
        .type(notification.getType())
        .content(notification.getContent())
        .referenceId(notification.getReferenceId())
        .createdAt(notification.getCreatedAt())
        .isRead(notification.isRead())
        .build();
  }

}