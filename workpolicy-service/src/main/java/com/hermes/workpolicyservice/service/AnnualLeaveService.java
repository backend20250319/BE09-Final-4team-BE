package com.hermes.workpolicyservice.service;

import com.hermes.workpolicyservice.dto.AnnualLeaveRequestDto;
import com.hermes.workpolicyservice.dto.AnnualLeaveResponseDto;
import com.hermes.workpolicyservice.dto.AnnualLeaveUpdateDto;

import java.util.List;

public interface AnnualLeaveService {
    
    /**
     * 연차 생성
     */
    AnnualLeaveResponseDto createAnnualLeave(Long workPolicyId, AnnualLeaveRequestDto requestDto);
    
    /**
     * 연차 조회 (ID로)
     */
    AnnualLeaveResponseDto getAnnualLeaveById(Long id);
    
    /**
     * 근무 정책의 연차 목록 조회
     */
    List<AnnualLeaveResponseDto> getAnnualLeavesByWorkPolicyId(Long workPolicyId);
    
    /**
     * 연차 수정
     */
    AnnualLeaveResponseDto updateAnnualLeave(Long id, AnnualLeaveUpdateDto updateDto);
    
    /**
     * 연차 삭제
     */
    void deleteAnnualLeave(Long id);
    
    /**
     * 근무 정책의 총 연차 일수 계산
     */
    Integer calculateTotalLeaveDays(Long workPolicyId);
    
    /**
     * 근무 정책의 총 휴가 일수 계산
     */
    Integer calculateTotalHolidayDays(Long workPolicyId);
} 