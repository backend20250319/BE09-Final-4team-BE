package com.hermes.communicationservice.announcement.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.communicationservice.announcement.dto.*;
import com.hermes.communicationservice.announcement.service.AnnouncementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

  private final AnnouncementService announcementService;

  // 공지사항 생성
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> createAnnouncement(
      @RequestBody AnnouncementCreateRequestDto request,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("POST /announcements 호출 - title: {}", request.getTitle());

    AnnouncementResponseDto response = announcementService.createAnnouncement(request,
        user.getId());

    log.info("공지사항 생성 완료 - id: {}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResult.success("공지사항 생성 완료", response));
  }

  // 공지사항 단건 조회
  @GetMapping("/{id}")
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> getAnnouncement(@PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /announcements/{} 호출", id);
    AnnouncementResponseDto response = announcementService.getAnnouncement(id);
    return ResponseEntity.ok(ApiResult.success("공지사항 조회 완료", response));
  }

  // 공지사항 전체 조회 (요약)
  @GetMapping
  public ResponseEntity<List<AnnouncementSummaryDto>> getAllAnnouncementsSummary(
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /announcements 호출 - 요약 목록 조회");
    List<AnnouncementSummaryDto> summary = announcementService.getAllAnnouncementSummary();
    return ResponseEntity.ok(summary);
  }

  // 공지사항 수정
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> updateAnnouncement(
      @PathVariable Long id,
      @RequestBody AnnouncementUpdateRequestDto request,
      @AuthenticationPrincipal UserPrincipal user) {

    log.info("PATCH /announcements/{} 호출", id);
    AnnouncementResponseDto updated = announcementService.updateAnnouncement(request, id,
        user.getId());
    return ResponseEntity.ok(ApiResult.success("공지사항 수정 완료", updated));

  }

  // 공지사항 삭제
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResult<Void>> deleteAnnouncement(@PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("DELETE /announcements/{} 호출", id);
    announcementService.deleteAnnouncement(id);
    return ResponseEntity.ok(ApiResult.success("공지사항 삭제 완료", null));
  }

}
