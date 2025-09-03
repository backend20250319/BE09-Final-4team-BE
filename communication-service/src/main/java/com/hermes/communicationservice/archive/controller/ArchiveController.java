package com.hermes.communicationservice.archive.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.communicationservice.archive.dto.ArchiveCreateRequestDto;
import com.hermes.communicationservice.archive.dto.ArchiveResponseDto;
import com.hermes.communicationservice.archive.dto.ArchiveSummaryDto;
import com.hermes.communicationservice.archive.dto.ArchiveUpdateRequestDto;
import com.hermes.communicationservice.archive.service.ArchiveService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사내 문서함 관리", description = "사내 문서 생성, 조회, 수정, 삭제 API")
public class ArchiveController {

  private final ArchiveService archiveService;

  @Operation(summary = "사내 문서 생성", description = "새로운 사내 문서를 생성합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "사내 문서 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ApiResult<ArchiveResponseDto>> createArchive(
      @Parameter(description = "사내 문서 생성 요청 정보", required = true) @Valid @RequestBody ArchiveCreateRequestDto request,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("POST /archives 호출 - title: {}", request.getTitle());

    ArchiveResponseDto response = archiveService.createArchive(request, user.getId());

    log.info("사내 문서 생성 완료 - id: {}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResult.success("사내 문서 생성 완료", response));
  }

  @Operation(summary = "사내 문서 상세 조회", description = "특정 사내 문서의 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사내 문서 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "사내 문서를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResult<ArchiveResponseDto>> getArchive(
      @Parameter(description = "사내 문서 ID", required = true, example = "1") @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /archives/{} 호출", id);
    ArchiveResponseDto response = archiveService.getArchive(id);
    return ResponseEntity.ok(ApiResult.success("사내 문서 조회 완료", response));
  }

  @Operation(summary = "사내 문서 목록 조회", description = "모든 사내 문서의 요약 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사내 문서 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public ResponseEntity<List<ArchiveSummaryDto>> getAllArchivesSummary(
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /archives 호출 - 요약 목록 조회");
    List<ArchiveSummaryDto> summary = archiveService.getAllArchivesSummary();
    return ResponseEntity.ok(summary);
  }

  @Operation(summary = "사내 문서 수정", description = "기존 사내 문서의 정보를 수정합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사내 문서 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "404", description = "사내 문서를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResult<ArchiveResponseDto>> updateArchive(
      @Parameter(description = "사내 문서 ID", required = true, example = "1") @PathVariable Long id,
      @Parameter(description = "사내 문서 수정 요청 정보", required = true) @Valid @RequestBody ArchiveUpdateRequestDto request,
      @AuthenticationPrincipal UserPrincipal user) {

    log.info("PATCH /archives/{} 호출", id);
    ArchiveResponseDto updated = archiveService.updateArchive(request, id, user.getId());
    return ResponseEntity.ok(ApiResult.success("사내 문서 수정 완료", updated));
  }

  @Operation(summary = "사내 문서 삭제", description = "기존 사내 문서를 삭제합니다. ADMIN 권한 필요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사내 문서 삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "403", description = "권한 부족 (ADMIN 권한 필요)"),
      @ApiResponse(responseCode = "404", description = "사내 문서를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResult<Void>> deleteArchive(
      @Parameter(description = "사내 문서 ID", required = true, example = "1") @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("DELETE /archives/{} 호출", id);
    archiveService.deleteArchive(id);
    return ResponseEntity.ok(ApiResult.success("사내 문서 삭제 완료", null));
  }

  @Operation(summary = "사내 문서 검색", description = "사내 문서 제목으로 검색합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사내 문서 검색 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/search")
  public ResponseEntity<ApiResult<List<ArchiveSummaryDto>>> searchArchives(
      @Parameter(description = "검색 키워드", required = true, example = "규정") @RequestParam("keyword") String keyword,
      @AuthenticationPrincipal UserPrincipal user) {
    log.info("GET /archives/search 호출 - keyword: {}", keyword);
    List<ArchiveSummaryDto> responses = archiveService.searchArchives(keyword);
    return ResponseEntity.ok(ApiResult.success(responses));
  }

}
