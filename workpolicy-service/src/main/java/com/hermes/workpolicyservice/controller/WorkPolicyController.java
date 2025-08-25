package com.hermes.workpolicyservice.controller;

import com.hermes.workpolicyservice.dto.*;
import com.hermes.workpolicyservice.service.WorkPolicyService;
import com.hermes.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/work-policies")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WorkPolicyController {
    
    private final WorkPolicyService workPolicyService;
    
    /**
     * 근무 정책 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkPolicyResponseDto>> createWorkPolicy(
            @Valid @RequestBody WorkPolicyRequestDto requestDto) {
        log.info("근무 정책 생성 요청: {}", requestDto.getName());
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.createWorkPolicy(requestDto);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("근무 정책이 성공적으로 생성되었습니다.", responseDto));
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("근무 정책 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책 조회 (ID로)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkPolicyResponseDto>> getWorkPolicyById(@PathVariable Long id) {
        log.info("근무 정책 조회 요청: ID={}", id);
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.getWorkPolicyById(id);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("근무 정책 조회 성공", responseDto));
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("근무 정책 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책 조회 (이름으로)
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<WorkPolicyResponseDto>> getWorkPolicyByName(@PathVariable String name) {
        log.info("근무 정책 조회 요청: 이름={}", name);
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.getWorkPolicyByName(name);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("근무 정책 조회 성공", responseDto));
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("근무 정책 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WorkPolicyListResponseDto>>> getWorkPolicyList(
            @ModelAttribute WorkPolicySearchDto searchDto) {
        log.info("근무 정책 목록 조회 요청: {}", searchDto);
        
        try {
            Page<WorkPolicyListResponseDto> responsePage = workPolicyService.getWorkPolicyList(searchDto);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("근무 정책 목록 조회 성공", responsePage));
                            
        } catch (Exception e) {
            log.error("근무 정책 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkPolicyResponseDto>> updateWorkPolicy(
            @PathVariable Long id,
            @Valid @RequestBody WorkPolicyUpdateDto updateDto) {
        log.info("근무 정책 수정 요청: ID={}", id);
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.updateWorkPolicy(id, updateDto);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("근무 정책이 성공적으로 수정되었습니다.", responseDto));
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("근무 정책 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkPolicy(@PathVariable Long id) {
        log.info("근무 정책 삭제 요청: ID={}", id);
        
        try {
            workPolicyService.deleteWorkPolicy(id);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("근무 정책이 성공적으로 삭제되었습니다."));
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 삭제 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("근무 정책 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    

    
    /**
     * 노동법 준수 여부 확인
     */
    @PostMapping("/labor-law-compliance")
    public ResponseEntity<ApiResponse<Boolean>> checkLaborLawCompliance(
            @Valid @RequestBody WorkPolicyRequestDto requestDto) {
        log.info("노동법 준수 여부 확인 요청: {}", requestDto.getName());
        
        try {
            boolean isCompliant = workPolicyService.checkLaborLawCompliance(requestDto);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            isCompliant ? "노동법을 준수합니다." : "노동법을 준수하지 않습니다.", 
                            isCompliant));
                            
        } catch (Exception e) {
            log.error("노동법 준수 여부 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
} 