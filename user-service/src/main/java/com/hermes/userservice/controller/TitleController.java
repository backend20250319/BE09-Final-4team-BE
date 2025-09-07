package com.hermes.userservice.controller;

import com.hermes.userservice.dto.title.*;
import com.hermes.userservice.service.TitleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/titles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Title Management", description = "직급/직위/직책 관리 API")
public class TitleController {

    private final TitleService titleService;

    // 직급 관련 API
    @GetMapping("/ranks")
    @Operation(summary = "모든 직급 조회", description = "시스템에 등록된 모든 직급을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직급 목록 조회 성공")
    public ResponseEntity<List<RankDto>> getAllRanks() {
        log.info("모든 직급 조회 API 호출");
        List<RankDto> ranks = titleService.getAllRanks();
        return ResponseEntity.ok(ranks);
    }

    @GetMapping("/ranks/{id}")
    @Operation(summary = "직급 상세 조회", description = "특정 직급의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직급 조회 성공")
    @ApiResponse(responseCode = "404", description = "직급을 찾을 수 없음")
    public ResponseEntity<RankDto> getRankById(
            @Parameter(description = "직급 ID", required = true) @PathVariable Long id) {
        log.info("직급 상세 조회 API 호출: id={}", id);
        RankDto rank = titleService.getRankById(id);
        return ResponseEntity.ok(rank);
    }

    @PostMapping("/ranks")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직급 생성", description = "새로운 직급을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "직급 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "409", description = "중복된 직급명")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<RankDto> createRank(
            @Valid @RequestBody CreateRankRequest request) {
        log.info("직급 생성 API 호출: name={}", request.getName());
        RankDto rank = titleService.createRank(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rank);
    }

    @PutMapping("/ranks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직급 수정", description = "기존 직급의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "직급 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "404", description = "직급을 찾을 수 없음")
    @ApiResponse(responseCode = "409", description = "중복된 직급명")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<RankDto> updateRank(
            @Parameter(description = "직급 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateRankRequest request) {
        log.info("직급 수정 API 호출: id={}, name={}", id, request.getName());
        RankDto rank = titleService.updateRank(id, request);
        return ResponseEntity.ok(rank);
    }

    @DeleteMapping("/ranks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직급 삭제", description = "기존 직급을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "직급 삭제 성공")
    @ApiResponse(responseCode = "404", description = "직급을 찾을 수 없음")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<Void> deleteRank(
            @Parameter(description = "직급 ID", required = true) @PathVariable Long id) {
        log.info("직급 삭제 API 호출: id={}", id);
        titleService.deleteRank(id);
        return ResponseEntity.ok().build();
    }

    // 직위 관련 API
    @GetMapping("/positions")
    @Operation(summary = "모든 직위 조회", description = "시스템에 등록된 모든 직위를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직위 목록 조회 성공")
    public ResponseEntity<List<PositionDto>> getAllPositions() {
        log.info("모든 직위 조회 API 호출");
        List<PositionDto> positions = titleService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/positions/{id}")
    @Operation(summary = "직위 상세 조회", description = "특정 직위의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직위 조회 성공")
    @ApiResponse(responseCode = "404", description = "직위를 찾을 수 없음")
    public ResponseEntity<PositionDto> getPositionById(
            @Parameter(description = "직위 ID", required = true) @PathVariable Long id) {
        log.info("직위 상세 조회 API 호출: id={}", id);
        PositionDto position = titleService.getPositionById(id);
        return ResponseEntity.ok(position);
    }

    @PostMapping("/positions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직위 생성", description = "새로운 직위를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "직위 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "409", description = "중복된 직위명")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<PositionDto> createPosition(
            @Valid @RequestBody CreatePositionRequest request) {
        log.info("직위 생성 API 호출: name={}", request.getName());
        PositionDto position = titleService.createPosition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(position);
    }

    @PutMapping("/positions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직위 수정", description = "기존 직위의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "직위 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "404", description = "직위를 찾을 수 없음")
    @ApiResponse(responseCode = "409", description = "중복된 직위명")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<PositionDto> updatePosition(
            @Parameter(description = "직위 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdatePositionRequest request) {
        log.info("직위 수정 API 호출: id={}, name={}", id, request.getName());
        PositionDto position = titleService.updatePosition(id, request);
        return ResponseEntity.ok(position);
    }

    @DeleteMapping("/positions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직위 삭제", description = "기존 직위를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "직위 삭제 성공")
    @ApiResponse(responseCode = "404", description = "직위를 찾을 수 없음")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<Void> deletePosition(
            @Parameter(description = "직위 ID", required = true) @PathVariable Long id) {
        log.info("직위 삭제 API 호출: id={}", id);
        titleService.deletePosition(id);
        return ResponseEntity.ok().build();
    }

    // 직책 관련 API
    @GetMapping("/jobs")
    @Operation(summary = "모든 직책 조회", description = "시스템에 등록된 모든 직책을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직책 목록 조회 성공")
    public ResponseEntity<List<JobDto>> getAllJobs() {
        log.info("모든 직책 조회 API 호출");
        List<JobDto> jobs = titleService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "직책 상세 조회", description = "특정 직책의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "직책 조회 성공")
    @ApiResponse(responseCode = "404", description = "직책을 찾을 수 없음")
    public ResponseEntity<JobDto> getJobById(
            @Parameter(description = "직책 ID", required = true) @PathVariable Long id) {
        log.info("직책 상세 조회 API 호출: id={}", id);
        JobDto job = titleService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직책 생성", description = "새로운 직책을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "직책 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "409", description = "중복된 직책명")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<JobDto> createJob(
            @Valid @RequestBody CreateJobRequest request) {
        log.info("직책 생성 API 호출: name={}", request.getName());
        JobDto job = titleService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직책 수정", description = "기존 직책의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "직책 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "404", description = "직책을 찾을 수 없음")
    @ApiResponse(responseCode = "409", description = "중복된 직책명")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<JobDto> updateJob(
            @Parameter(description = "직책 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request) {
        log.info("직책 수정 API 호출: id={}, name={}", id, request.getName());
        JobDto job = titleService.updateJob(id, request);
        return ResponseEntity.ok(job);
    }

    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "직책 삭제", description = "기존 직책을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "직책 삭제 성공")
    @ApiResponse(responseCode = "404", description = "직책을 찾을 수 없음")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "직책 ID", required = true) @PathVariable Long id) {
        log.info("직책 삭제 API 호출: id={}", id);
        titleService.deleteJob(id);
        return ResponseEntity.ok().build();
    }
}
