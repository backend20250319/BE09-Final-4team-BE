package com.hermes.attendanceservice.service;

import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveRequestDto;
import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveResponseDto;
import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveUpdateDto;

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
     * 근무 정책별 연차 목록 조회
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
     * 근무 정책별 총 연차 일수 계산
     */
    Integer calculateTotalLeaveDays(Long workPolicyId);
    
    /**
     * 근무 정책별 총 휴일 일수 계산
     */
    Integer calculateTotalHolidayDays(Long workPolicyId);
} 
