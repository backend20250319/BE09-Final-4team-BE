package com.hermes.communicationservice.announcement.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.communicationservice.announcement.dto.*;
import com.hermes.communicationservice.announcement.service.AnnouncementService;
import com.hermes.communicationservice.file.dto.FileMappingDto;
import com.hermes.communicationservice.file.service.FileMappingService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

  private final AnnouncementService announcementService;
  // private final FileMappingService fileMappingService;

  // 공지사항 생성
  @PostMapping
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> createAnnouncement(
      @ModelAttribute AnnouncementCreateRequestDto request) {
    log.info("POST /announcements 호출 - title: {}", request.getTitle());

    AnnouncementResponseDto response = announcementService.createAnnouncement(request);

    log.info("공지사항 생성 완료 - id: {}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResult.success("공지사항 생성 완료", response));
  }

  // 공지사항 단건 조회
  @GetMapping("/{id}")
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> getAnnouncement(@PathVariable Long id) {
    log.info("GET /announcements/{} 호출", id);
    AnnouncementResponseDto response = announcementService.getAnnouncement(id);
    return ResponseEntity.ok(ApiResult.success("공지사항 조회 완료", response));
  }

  // 공지사항 전체 조회 (요약)
  @GetMapping
  public ResponseEntity<List<AnnouncementSummaryDto>> getAllAnnouncementsSummary() {
    log.info("GET /announcements 호출 - 요약 목록 조회");
    List<AnnouncementSummaryDto> summary = announcementService.getAllAnnouncementSummary();
    return ResponseEntity.ok(summary);
  }

  // 공지사항 수정
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> updateAnnouncement(
      @PathVariable Long id,
      @ModelAttribute AnnouncementUpdateRequestDto request) {

    log.info("PATCH /announcements/{} 호출", id);
    AnnouncementResponseDto updated = announcementService.updateAnnouncement(request);
    return ResponseEntity.ok(ApiResult.success("공지사항 수정 완료", updated));

  }

  // 공지사항 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResult<Void>> deleteAnnouncement(@PathVariable Long id) {
    log.info("DELETE /announcements/{} 호출", id);
    announcementService.deleteAnnouncement(id);
    return ResponseEntity.ok(ApiResult.success("공지사항 삭제 완료", null));
  }

}
