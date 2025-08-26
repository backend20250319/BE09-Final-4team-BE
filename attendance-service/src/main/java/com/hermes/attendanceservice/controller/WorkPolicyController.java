package com.hermes.attendanceservice.controller;

import com.hermes.attendanceservice.dto.workpolicy.*;
import com.hermes.attendanceservice.service.WorkPolicyService;
import com.hermes.api.common.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-policies")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WorkPolicyController {
    
    private final WorkPolicyService workPolicyService;
    
    /**
     * 근무 정책 생성
     */
    @PostMapping
    public ApiResult<WorkPolicyResponseDto> createWorkPolicy(
            @Valid @RequestBody WorkPolicyRequestDto requestDto) {
        log.info("근무 정책 생성 요청: {}", requestDto.getName());
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.createWorkPolicy(requestDto);
            
            return ApiResult.success("근무 정책이 성공적으로 생성되었습니다.", responseDto);
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 생성 실패: {}", e.getMessage());
            return ApiResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("근무 정책 생성 예외 발생", e);
            return ApiResult.failure("서버 내부 오류가 발생했습니다.");
        }
    }
    
    /**
     * 근무 정책 조회 (ID로)
     */
    @GetMapping("/{id}")
    public ApiResult<WorkPolicyResponseDto> getWorkPolicyById(@PathVariable Long id) {
        log.info("근무 정책 조회 요청: ID={}", id);
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.getWorkPolicyById(id);
            
            return ApiResult.success("근무 정책 조회 성공", responseDto);
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 조회 실패: {}", e.getMessage());
            return ApiResult.failure("근무 정책을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("근무 정책 조회 예외 발생", e);
            return ApiResult.failure("서버 내부 오류가 발생했습니다.");
        }
    }
    
    /**
     * 근무 정책 조회 (이름으로)
     */
    @GetMapping("/name/{name}")
    public ApiResult<WorkPolicyResponseDto> getWorkPolicyByName(@PathVariable String name) {
        log.info("근무 정책 조회 요청: 이름={}", name);
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.getWorkPolicyByName(name);
            
            return ApiResult.success("근무 정책 조회 성공", responseDto);
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 조회 실패: {}", e.getMessage());
            return ApiResult.failure("근무 정책을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("근무 정책 조회 예외 발생", e);
            return ApiResult.failure("서버 내부 오류가 발생했습니다.");
        }
    }
    
    /**
     * 근무 정책 목록 조회 (페이징)
     */
    @GetMapping
    public ApiResult<Page<WorkPolicyListResponseDto>> getWorkPolicyList(
            @ModelAttribute WorkPolicySearchDto searchDto) {
        log.info("근무 정책 목록 조회 요청: {}", searchDto);
        
        try {
            Page<WorkPolicyListResponseDto> responseDto = workPolicyService.getWorkPolicyList(searchDto);
            
            return ApiResult.success("근무 정책 목록 조회 성공", responseDto);
                            
        } catch (Exception e) {
            log.error("근무 정책 목록 조회 예외 발생", e);
            return ApiResult.failure("서버 내부 오류가 발생했습니다.");
        }
    }
    
    /**
     * 근무 정책 수정
     */
    @PutMapping("/{id}")
    public ApiResult<WorkPolicyResponseDto> updateWorkPolicy(
            @PathVariable Long id,
            @Valid @RequestBody WorkPolicyUpdateDto updateDto) {
        log.info("근무 정책 수정 요청: ID={}", id);
        
        try {
            WorkPolicyResponseDto responseDto = workPolicyService.updateWorkPolicy(id, updateDto);
            
            return ApiResult.success("근무 정책이 성공적으로 수정되었습니다.", responseDto);
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 수정 실패: {}", e.getMessage());
            return ApiResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("근무 정책 수정 예외 발생", e);
            return ApiResult.failure("서버 내부 오류가 발생했습니다.");
        }
    }
    
    /**
     * 근무 정책 삭제
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteWorkPolicy(@PathVariable Long id) {
        log.info("근무 정책 삭제 요청: ID={}", id);
        
        try {
            workPolicyService.deleteWorkPolicy(id);
            
            return ApiResult.success("근무 정책이 성공적으로 삭제되었습니다.", null);
                            
        } catch (IllegalArgumentException e) {
            log.warn("근무 정책 삭제 실패: {}", e.getMessage());
            return ApiResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("근무 정책 삭제 예외 발생", e);
            return ApiResult.failure("서버 내부 오류가 발생했습니다.");
        }
    }
} 
