package com.hermes.notification.event;

import com.hermes.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

  private Long userId;                  // 알림을 받을 사용자 ID
  private NotificationType type;        // 알림 타입 (공지사항, 결재 등)
  private String content;               // 공지사항 내용 ("하반기 인사발령", "휴가 신청서")
  private Long referenceId;             // 관련 리소스 ID (공지사항 id or 결재 id)
  private LocalDateTime createdAt;      // 이벤트 발생 시간

}