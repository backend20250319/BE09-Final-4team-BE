package com.hermes.attendanceservice.service.workpolicy;

import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveRequestDto;
import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveResponseDto;
import com.hermes.attendanceservice.dto.workpolicy.AnnualLeaveUpdateDto;
import com.hermes.attendanceservice.entity.workpolicy.AnnualLeave;
import com.hermes.attendanceservice.entity.workpolicy.WorkPolicy;
import com.hermes.attendanceservice.repository.workpolicy.AnnualLeaveRepository;
import com.hermes.attendanceservice.repository.workpolicy.WorkPolicyRepository;
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
        log.info("연차 생성 시작: workPolicyId={}, name={}", workPolicyId, requestDto.getName());
        
        WorkPolicy workPolicy = workPolicyRepository.findById(workPolicyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근무 정책입니다: " + workPolicyId));
        
        AnnualLeave annualLeave = AnnualLeave.builder()
                .workPolicy(workPolicy)
                .name(requestDto.getName())
                .minYears(requestDto.getMinYears())
                .maxYears(requestDto.getMaxYears())
                .leaveDays(requestDto.getLeaveDays())
                .holidayDays(requestDto.getHolidayDays())
                .build();
        
        AnnualLeave savedLeave = annualLeaveRepository.save(annualLeave);
        log.info("연차 생성 완료: ID={}, name={}", savedLeave.getId(), savedLeave.getName());
        
        return convertToResponseDto(savedLeave);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AnnualLeaveResponseDto getAnnualLeaveById(Long id) {
        log.info("연차 조회 시작: ID={}", id);
        
        AnnualLeave annualLeave = annualLeaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연차입니다: " + id));
        
        log.info("연차 조회 완료: ID={}, name={}", id, annualLeave.getName());
        return convertToResponseDto(annualLeave);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnualLeaveResponseDto> getAnnualLeavesByWorkPolicyId(Long workPolicyId) {
        log.info("근무 정책 연차 목록 조회 시작: workPolicyId={}", workPolicyId);
        
        List<AnnualLeave> annualLeaves = annualLeaveRepository.findByWorkPolicyId(workPolicyId);
        
        List<AnnualLeaveResponseDto> responseDtos = annualLeaves.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        
        log.info("근무 정책 연차 목록 조회 완료: workPolicyId={}, count={}", workPolicyId, responseDtos.size());
        return responseDtos;
    }
    
    @Override
    public AnnualLeaveResponseDto updateAnnualLeave(Long id, AnnualLeaveUpdateDto updateDto) {
        log.info("연차 수정 시작: ID={}", id);
        
        AnnualLeave annualLeave = annualLeaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연차입니다: " + id));
        
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
        
        AnnualLeave updatedLeave = annualLeaveRepository.save(annualLeave);
        log.info("연차 수정 완료: ID={}, name={}", id, updatedLeave.getName());
        
        return convertToResponseDto(updatedLeave);
    }
    
    @Override
    public void deleteAnnualLeave(Long id) {
        log.info("연차 삭제 시작: ID={}", id);
        
        AnnualLeave annualLeave = annualLeaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연차입니다: " + id));
        
        annualLeaveRepository.delete(annualLeave);
        log.info("연차 삭제 완료: ID={}, name={}", id, annualLeave.getName());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer calculateTotalLeaveDays(Long workPolicyId) {
        return annualLeaveRepository.calculateTotalLeaveDaysByWorkPolicyId(workPolicyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer calculateTotalHolidayDays(Long workPolicyId) {
        return annualLeaveRepository.calculateTotalHolidayDaysByWorkPolicyId(workPolicyId);
    }
    
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