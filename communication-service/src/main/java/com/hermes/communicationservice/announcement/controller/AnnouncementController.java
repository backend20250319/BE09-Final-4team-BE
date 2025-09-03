package com.hermes.communicationservice.announcement.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.communicationservice.announcement.dto.*;
import com.hermes.communicationservice.announcement.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "공지사항 관리", description = "공지사항 생성, 조회, 수정, 삭제 API")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @Operation(summary = "공지사항 생성", description = "새로운 공지사항을 생성합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "공지사항 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> createAnnouncement(
      @Parameter(description = "공지사항 생성 요청 정보", required = true) @Valid @RequestBody AnnouncementCreateRequestDto request,
      @AuthenticationPrincipal UserPrincipal user,
      @RequestHeader("Authorization") String authorization) {
    log.info("POST /announcements 호출 - title: {}", request.getTitle());

    AnnouncementResponseDto response =
        announcementService.createAnnouncement(request, user.getId(), authorization);

    log.info("공지사항 생성 완료 - id: {}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResult.success("공지사항 생성 완료", response));
  }

  @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "공지사항 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> getAnnouncement(
      @Parameter(description = "공지사항 ID", required = true, example = "1") @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /announcements/{} 호출", id);
    AnnouncementResponseDto response = announcementService.getAnnouncement(id);
    return ResponseEntity.ok(ApiResult.success("공지사항 조회 완료", response));
  }

  @Operation(summary = "공지사항 목록 조회", description = "모든 공지사항의 요약 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<List<AnnouncementSummaryDto>> getAllAnnouncementsSummary(
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /announcements 호출 - 요약 목록 조회");
    List<AnnouncementSummaryDto> summary = announcementService.getAllAnnouncementSummary();
    return ResponseEntity.ok(summary);
  }

  @Operation(summary = "공지사항 수정", description = "기존 공지사항의 정보를 수정합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "공지사항 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResult<AnnouncementResponseDto>> updateAnnouncement(
      @Parameter(description = "공지사항 ID", required = true, example = "1") @PathVariable Long id,
      @Parameter(description = "공지사항 수정 요청 정보", required = true) @Valid @RequestBody AnnouncementUpdateRequestDto request,
      @AuthenticationPrincipal UserPrincipal user) {

    log.info("PATCH /announcements/{} 호출", id);
    AnnouncementResponseDto updated = announcementService.updateAnnouncement(request, id,
        user.getId());
    return ResponseEntity.ok(ApiResult.success("공지사항 수정 완료", updated));

  }

  @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "공지사항 삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResult<Void>> deleteAnnouncement(
      @Parameter(description = "공지사항 ID", required = true, example = "1") @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("DELETE /announcements/{} 호출", id);
    announcementService.deleteAnnouncement(id);
    return ResponseEntity.ok(ApiResult.success("공지사항 삭제 완료", null));
  }

  @Operation(summary = "공지사항 검색", description = "공지사항 제목으로 검색합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "공지사항 검색 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족"),
      @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/search")
  public ResponseEntity<ApiResult<List<AnnouncementSummaryDto>>> searchAnnouncements(@RequestParam("keyword") String keyword) {
    List<AnnouncementSummaryDto> responses = announcementService.searchAnnouncement(keyword);
    return ResponseEntity.ok(ApiResult.success(responses));
  }

}
