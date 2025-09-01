package com.hermes.notification.dto;

import com.hermes.notification.enums.NotificationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationRequest {

  @NotEmpty(message = "알림 수신자 목록은 필수입니다.")
  private List<Long> userIds;          // 알림을 받을 사용자 ID 목록

  @NotNull(message = "알림 타입은 필수입니다.")
  private NotificationType type;       // 알림 타입

  private String content;              // 알림 내용

  private Long referenceId;            // 관련 리소스 ID (공지사항 ID, 결재문서 ID)

  @NotNull(message = "알림 이벤트 발생 시간은 필수입니다.")
  private LocalDateTime createdAt;     // 이벤트 발생 시간
}
