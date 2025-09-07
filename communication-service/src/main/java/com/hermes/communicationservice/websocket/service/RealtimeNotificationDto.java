package com.hermes.communicationservice.websocket.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 클라이언트에게 웹소켓으로 보낼 알림
public class RealtimeNotificationDto {

  private Long notificationId; // 알림 고유 ID (DB에 저장된 알림의 PK)
  private Long userId; // 알림 대상 사용자 ID (전사원 알림이면 null)
  private String type; // 알림 타입 (ANNOUNCEMENT, APPROVAL_REQUEST 등)
  private String content; // 알림 내용
  private Long referenceId; // 참조 ID (공지사항 ID, 결재 문서 ID 등)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant createdAt; // 참조 ID (공지사항 ID, 결재 문서 ID 등)
}
