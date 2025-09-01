package com.hermes.communicationservice.notification.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.communicationservice.notification.dto.NotificationResponseDto;
import com.hermes.communicationservice.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

  private final NotificationService notificationService;


  // 내 알림 목록 조회 (커서 페이징)
  @GetMapping
  public ResponseEntity<ApiResult<List<NotificationResponseDto>>> getMyNotifications(
      @AuthenticationPrincipal UserPrincipal user,
      @RequestParam(required = false) Long lastId,
      @RequestParam(defaultValue = "20") int size) {
    log.info("GET /notifications 호출 - userId: {}, lastId: {}, size: {}", user.getUserId(), lastId,
        size);

    List<NotificationResponseDto> notifications = notificationService.getNotifications(
        user.getUserId(), lastId, size);

    log.info("알림 목록 조회 완료 - count: {}", notifications.size());
    return ResponseEntity.ok(ApiResult.success("알림 목록 조회 완료", notifications));
  }

  // 읽지 않은 알림 존재 여부 확인
  @GetMapping("/unread")
  public ResponseEntity<ApiResult<Boolean>> hasUnreadNotifications(
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /notifications/unread 호출 - userId: {}", user.getUserId());

    boolean hasUnread = notificationService.hasUnreadNotifications(user.getUserId());

    log.info("읽지 않은 알림 존재 확인 완료 - hasUnread: {}", hasUnread);
    return ResponseEntity.ok(ApiResult.success("읽지 않은 알림 존재 확인 완료", hasUnread));
  }

  // 알림 읽음 처리
  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResult<Boolean>> markAsRead(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("PATCH /notifications/{}/read 호출 - userId: {}", id, user.getUserId());

    boolean success = notificationService.markAsRead(id, user.getUserId());

    log.info("알림 읽음 처리 완료 - success: {}", success);
    return ResponseEntity.ok(ApiResult.success("알림 읽음 처리 완료", success));
  }


  // 특정 사용자 알림 목록 조회 (관리자만 가능)
  @GetMapping("/admin/users/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResult<List<NotificationResponseDto>>> getUserNotifications(
      @PathVariable Long userId,
      @RequestParam(required = false) Long lastId,
      @RequestParam(defaultValue = "20") int size) {
    log.info("GET /notifications/admin/users/{} 호출 - lastId: {}, size: {}", userId, lastId, size);

    List<NotificationResponseDto> notifications = notificationService.getNotifications(userId,
        lastId, size);

    log.info("사용자 알림 목록 조회 완료 - userId: {}, count: {}", userId, notifications.size());
    return ResponseEntity.ok(ApiResult.success("사용자 알림 목록 조회 완료", notifications));
  }

  // 특정 사용자 읽지 않은 알림 존재 여부 확인 (관리자만 가능)
  @GetMapping("/admin/users/{userId}/unread")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResult<Boolean>> hasUserUnreadNotifications(@PathVariable Long userId) {
    log.info("GET /notifications/admin/users/{}/unread 호출", userId);

    boolean hasUnread = notificationService.hasUnreadNotifications(userId);

    log.info("사용자 읽지 않은 알림 존재 확인 완료 - userId: {}, hasUnread: {}", userId, hasUnread);
    return ResponseEntity.ok(ApiResult.success("사용자 읽지 않은 알림 존재 확인 완료", hasUnread));
  }

}