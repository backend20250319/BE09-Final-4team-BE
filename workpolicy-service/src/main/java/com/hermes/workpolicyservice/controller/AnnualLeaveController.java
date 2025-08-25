package com.hermes.workpolicyservice.controller;

import com.hermes.workpolicyservice.dto.*;
import com.hermes.workpolicyservice.service.AnnualLeaveService;
import com.hermes.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/work-policies/{workPolicyId}/annual-leaves")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AnnualLeaveController {
    
    private final AnnualLeaveService annualLeaveService;
    
    /**
     * 연차 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AnnualLeaveResponseDto>> createAnnualLeave(
            @PathVariable Long workPolicyId,
            @Valid @RequestBody AnnualLeaveRequestDto requestDto) {
        log.info("연차 생성 요청: 근무정책ID={}, 연차명={}", workPolicyId, requestDto.getName());
        
        try {
            AnnualLeaveResponseDto responseDto = annualLeaveService.createAnnualLeave(workPolicyId, requestDto);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("연차가 성공적으로 생성되었습니다.", responseDto));
                            
        } catch (IllegalArgumentException e) {
            log.warn("연차 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("연차 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 연차 조회 (ID로)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnualLeaveResponseDto>> getAnnualLeaveById(@PathVariable Long id) {
        log.info("연차 조회 요청: ID={}", id);
        
        try {
            AnnualLeaveResponseDto responseDto = annualLeaveService.getAnnualLeaveById(id);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("연차 조회 성공", responseDto));
                            
        } catch (IllegalArgumentException e) {
            log.warn("연차 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("연차 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책의 연차 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnualLeaveResponseDto>>> getAnnualLeavesByWorkPolicyId(
            @PathVariable Long workPolicyId) {
        log.info("근무 정책 연차 목록 조회 요청: 근무정책ID={}", workPolicyId);
        
        try {
            List<AnnualLeaveResponseDto> responseList = annualLeaveService.getAnnualLeavesByWorkPolicyId(workPolicyId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("연차 목록 조회 성공", responseList));
                            
        } catch (Exception e) {
            log.error("연차 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 연차 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnualLeaveResponseDto>> updateAnnualLeave(
            @PathVariable Long id,
            @Valid @RequestBody AnnualLeaveUpdateDto updateDto) {
        log.info("연차 수정 요청: ID={}", id);
        
        try {
            AnnualLeaveResponseDto responseDto = annualLeaveService.updateAnnualLeave(id, updateDto);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("연차가 성공적으로 수정되었습니다.", responseDto));
                            
        } catch (IllegalArgumentException e) {
            log.warn("연차 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("연차 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 연차 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnualLeave(@PathVariable Long id) {
        log.info("연차 삭제 요청: ID={}", id);
        
        try {
            annualLeaveService.deleteAnnualLeave(id);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("연차가 성공적으로 삭제되었습니다."));
                            
        } catch (IllegalArgumentException e) {
            log.warn("연차 삭제 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("연차 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책의 총 연차 일수 조회
     */
    @GetMapping("/total-leave-days")
    public ResponseEntity<ApiResponse<Integer>> getTotalLeaveDays(@PathVariable Long workPolicyId) {
        log.info("총 연차 일수 조회 요청: 근무정책ID={}", workPolicyId);
        
        try {
            Integer totalDays = annualLeaveService.calculateTotalLeaveDays(workPolicyId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("총 연차 일수 조회 성공", totalDays));
                            
        } catch (Exception e) {
            log.error("총 연차 일수 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
    
    /**
     * 근무 정책의 총 휴가 일수 조회
     */
    @GetMapping("/total-holiday-days")
    public ResponseEntity<ApiResponse<Integer>> getTotalHolidayDays(@PathVariable Long workPolicyId) {
        log.info("총 휴가 일수 조회 요청: 근무정책ID={}", workPolicyId);
        
        try {
            Integer totalDays = annualLeaveService.calculateTotalHolidayDays(workPolicyId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success("총 휴가 일수 조회 성공", totalDays));
                            
        } catch (Exception e) {
            log.error("총 휴가 일수 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다."));
        }
    }
} 