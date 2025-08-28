package com.hermes.attendanceservice.service.workpolicy;

import com.hermes.attendanceservice.dto.workpolicy.*;
import org.springframework.data.domain.Page;

public interface WorkPolicyService {
    
    /**
     * 근무 정책 생성
     */
    WorkPolicyResponseDto createWorkPolicy(WorkPolicyRequestDto requestDto);
    
    /**
     * 근무 정책 조회 (ID로)
     */
    WorkPolicyResponseDto getWorkPolicyById(Long id);
    
    /**
     * 근무 정책 조회 (이름으로)
     */
    WorkPolicyResponseDto getWorkPolicyByName(String name);
    
    /**
     * 근무 정책 목록 조회 (페이징)
     */
    Page<WorkPolicyListResponseDto> getWorkPolicyList(WorkPolicySearchDto searchDto);
    
    /**
     * 근무 정책 수정
     */
    WorkPolicyResponseDto updateWorkPolicy(Long id, WorkPolicyUpdateDto updateDto);
    
    /**
     * 근무 정책 삭제
     */
    void deleteWorkPolicy(Long id);
    
    /**
     * 노동법 준수 여부 확인
     */
    boolean checkLaborLawCompliance(WorkPolicyRequestDto requestDto);
} 