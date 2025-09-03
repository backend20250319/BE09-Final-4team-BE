package com.hermes.communicationservice.notification.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.communicationservice.notification.dto.NotificationResponseDto;
import com.hermes.communicationservice.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "알림 관리", description = "사용자 알림 조회, 읽음 처리 API")
public class NotificationController {

  private final NotificationService notificationService;


  @Operation(summary = "내 알림 목록 조회", description = "현재 사용자의 알림 목록을 커서 페이징으로 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<ApiResult<List<NotificationResponseDto>>> getMyNotifications(
      @AuthenticationPrincipal UserPrincipal user,
      @Parameter(description = "마지막으로 조회한 알림 ID (커서)", example = "100") @RequestParam(required = false) Long lastId,
      @Parameter(description = "조회할 알림 개수", example = "20") @RequestParam(defaultValue = "20") int size) {
    log.info("GET /notifications 호출 - userId: {}, lastId: {}, size: {}", user.getId(), lastId,
        size);

    List<NotificationResponseDto> notifications = notificationService.getNotifications(
        user.getId(), lastId, size);

    log.info("알림 목록 조회 완료 - count: {}", notifications.size());
    return ResponseEntity.ok(ApiResult.success("알림 목록 조회 완료", notifications));
  }

  @Operation(summary = "읽지 않은 알림 존재 확인", description = "현재 사용자의 읽지 않은 알림이 있는지 확인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "확인 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/unread")
  public ResponseEntity<ApiResult<Boolean>> hasUnreadNotifications(
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /notifications/unread 호출 - userId: {}", user.getId());

    boolean hasUnread = notificationService.hasUnreadNotifications(user.getId());

    log.info("읽지 않은 알림 존재 확인 완료 - hasUnread: {}", hasUnread);
    return ResponseEntity.ok(ApiResult.success("읽지 않은 알림 존재 확인 완료", hasUnread));
  }

  @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResult<Boolean>> markAsRead(
      @Parameter(description = "알림 ID", required = true, example = "1") @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("PATCH /notifications/{}/read 호출 - userId: {}", id, user.getId());

    boolean success = notificationService.markAsRead(id, user.getId());

    log.info("알림 읽음 처리 완료 - success: {}", success);
    return ResponseEntity.ok(ApiResult.success("알림 읽음 처리 완료", success));
  }


  @Operation(summary = "특정 사용자 알림 목록 조회", description = "관리자가 특정 사용자의 알림 목록을 조회합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/admin/users/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResult<List<NotificationResponseDto>>> getUserNotifications(
      @Parameter(description = "조회할 사용자 ID", required = true, example = "1") @PathVariable Long userId,
      @Parameter(description = "마지막으로 조회한 알림 ID (커서)", example = "100") @RequestParam(required = false) Long lastId,
      @Parameter(description = "조회할 알림 개수", example = "20") @RequestParam(defaultValue = "20") int size) {
    log.info("GET /notifications/admin/users/{} 호출 - lastId: {}, size: {}", userId, lastId, size);

    List<NotificationResponseDto> notifications = notificationService.getNotifications(userId,
        lastId, size);

    log.info("사용자 알림 목록 조회 완료 - userId: {}, count: {}", userId, notifications.size());
    return ResponseEntity.ok(ApiResult.success("사용자 알림 목록 조회 완료", notifications));
  }

  @Operation(summary = "특정 사용자 읽지 않은 알림 존재 확인", description = "관리자가 특정 사용자의 읽지 않은 알림 존재 여부를 확인합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "확인 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/admin/users/{userId}/unread")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResult<Boolean>> hasUserUnreadNotifications(
      @Parameter(description = "조회할 사용자 ID", required = true, example = "1") @PathVariable Long userId) {
    log.info("GET /notifications/admin/users/{}/unread 호출", userId);

    boolean hasUnread = notificationService.hasUnreadNotifications(userId);

    log.info("사용자 읽지 않은 알림 존재 확인 완료 - userId: {}, hasUnread: {}", userId, hasUnread);
    return ResponseEntity.ok(ApiResult.success("사용자 읽지 않은 알림 존재 확인 완료", hasUnread));
  }

}