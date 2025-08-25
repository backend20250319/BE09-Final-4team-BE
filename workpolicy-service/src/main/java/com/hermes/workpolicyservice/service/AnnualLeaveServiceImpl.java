package com.hermes.workpolicyservice.service;

import com.hermes.workpolicyservice.dto.AnnualLeaveRequestDto;
import com.hermes.workpolicyservice.dto.AnnualLeaveResponseDto;
import com.hermes.workpolicyservice.dto.AnnualLeaveUpdateDto;
import com.hermes.workpolicyservice.entity.AnnualLeave;
import com.hermes.workpolicyservice.entity.WorkPolicy;
import com.hermes.workpolicyservice.repository.AnnualLeaveRepository;
import com.hermes.workpolicyservice.repository.WorkPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnnualLeaveServiceImpl implements AnnualLeaveService {
    
    private final AnnualLeaveRepository annualLeaveRepository;
    private final WorkPolicyRepository workPolicyRepository;
    
    @Override
    public AnnualLeaveResponseDto createAnnualLeave(Long workPolicyId, AnnualLeaveRequestDto requestDto) {
        log.info("연차 생성 시작: 근무정책ID={}, 연차명={}", workPolicyId, requestDto.getName());
        
        // 근무 정책 존재 확인
        WorkPolicy workPolicy = workPolicyRepository.findById(workPolicyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근무 정책입니다: " + workPolicyId));
        
        // 범위 중복 검증
        validateRangeOverlap(workPolicyId, requestDto.getMinYears(), requestDto.getMaxYears());
        
        // 연차 생성
        AnnualLeave annualLeave = AnnualLeave.builder()
                .workPolicy(workPolicy)
                .name(requestDto.getName())
                .minYears(requestDto.getMinYears())
                .maxYears(requestDto.getMaxYears())
                .leaveDays(requestDto.getLeaveDays())
                .holidayDays(requestDto.getHolidayDays())
                .build();
        
        AnnualLeave savedAnnualLeave = annualLeaveRepository.save(annualLeave);
        log.info("연차 생성 완료: ID={}, 이름={}", savedAnnualLeave.getId(), savedAnnualLeave.getName());
        
        return convertToResponseDto(savedAnnualLeave);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AnnualLeaveResponseDto getAnnualLeaveById(Long id) {
        log.info("연차 조회 시작: ID={}", id);
        
        AnnualLeave annualLeave = annualLeaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연차입니다: " + id));
        
        log.info("연차 조회 완료: ID={}, 이름={}", id, annualLeave.getName());
        return convertToResponseDto(annualLeave);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnualLeaveResponseDto> getAnnualLeavesByWorkPolicyId(Long workPolicyId) {
        log.info("근무 정책 연차 목록 조회 시작: 근무정책ID={}", workPolicyId);
        
        List<AnnualLeave> annualLeaves = annualLeaveRepository.findByWorkPolicyId(workPolicyId);
        
        log.info("근무 정책 연차 목록 조회 완료: 근무정책ID={}, 연차수={}", workPolicyId, annualLeaves.size());
        return annualLeaves.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public AnnualLeaveResponseDto updateAnnualLeave(Long id, AnnualLeaveUpdateDto updateDto) {
        log.info("연차 수정 시작: ID={}", id);
        
        AnnualLeave annualLeave = annualLeaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연차입니다: " + id));
        
        // 필드 업데이트
        if (updateDto.getName() != null) {
            annualLeave.setName(updateDto.getName());
        }
        if (updateDto.getMinYears() != null) {
            annualLeave.setMinYears(updateDto.getMinYears());
        }
        if (updateDto.getMaxYears() != null) {
            annualLeave.setMaxYears(updateDto.getMaxYears());
        }
        if (updateDto.getLeaveDays() != null) {
            annualLeave.setLeaveDays(updateDto.getLeaveDays());
        }
        if (updateDto.getHolidayDays() != null) {
            annualLeave.setHolidayDays(updateDto.getHolidayDays());
        }
        
        AnnualLeave updatedAnnualLeave = annualLeaveRepository.save(annualLeave);
        log.info("연차 수정 완료: ID={}, 이름={}", id, updatedAnnualLeave.getName());
        
        return convertToResponseDto(updatedAnnualLeave);
    }
    
    @Override
    public void deleteAnnualLeave(Long id) {
        log.info("연차 삭제 시작: ID={}", id);
        
        AnnualLeave annualLeave = annualLeaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연차입니다: " + id));
        
        annualLeaveRepository.delete(annualLeave);
        log.info("연차 삭제 완료: ID={}, 이름={}", id, annualLeave.getName());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer calculateTotalLeaveDays(Long workPolicyId) {
        log.debug("총 연차 일수 계산: 근무정책ID={}", workPolicyId);
        
        Integer totalDays = annualLeaveRepository.calculateTotalLeaveDaysByWorkPolicyId(workPolicyId);
        log.debug("총 연차 일수 계산 완료: 근무정책ID={}, 총일수={}", workPolicyId, totalDays);
        
        return totalDays;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer calculateTotalHolidayDays(Long workPolicyId) {
        log.debug("총 휴가 일수 계산: 근무정책ID={}", workPolicyId);
        
        Integer totalDays = annualLeaveRepository.calculateTotalHolidayDaysByWorkPolicyId(workPolicyId);
        log.debug("총 휴가 일수 계산 완료: 근무정책ID={}, 총일수={}", workPolicyId, totalDays);
        
        return totalDays;
    }
    
    /**
     * 범위 중복 검증
     */
    private void validateRangeOverlap(Long workPolicyId, Integer minYears, Integer maxYears) {
        List<AnnualLeave> existingLeaves = annualLeaveRepository.findByWorkPolicyId(workPolicyId);
        
        for (AnnualLeave existing : existingLeaves) {
            if ((minYears <= existing.getMaxYears() && maxYears >= existing.getMinYears())) {
                throw new IllegalArgumentException("연차 범위가 중복됩니다: " + 
                    existing.getMinYears() + "~" + existing.getMaxYears() + "년차");
            }
        }
    }
    
    // DTO 변환 메서드
    private AnnualLeaveResponseDto convertToResponseDto(AnnualLeave annualLeave) {
        return AnnualLeaveResponseDto.builder()
                .id(annualLeave.getId())
                .workPolicyId(annualLeave.getWorkPolicy().getId())
                .name(annualLeave.getName())
                .minYears(annualLeave.getMinYears())
                .maxYears(annualLeave.getMaxYears())
                .leaveDays(annualLeave.getLeaveDays())
                .holidayDays(annualLeave.getHolidayDays())
                .rangeDescription(annualLeave.getRangeDescription())
                .createdAt(annualLeave.getCreatedAt())
                .updatedAt(annualLeave.getUpdatedAt())
                .build();
    }
} 