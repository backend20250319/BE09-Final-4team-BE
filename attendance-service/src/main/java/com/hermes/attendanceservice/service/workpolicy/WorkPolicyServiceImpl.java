package com.hermes.attendanceservice.service.workpolicy;

import com.hermes.attendanceservice.dto.workpolicy.*;
import com.hermes.attendanceservice.entity.workpolicy.AnnualLeave;
import com.hermes.attendanceservice.entity.workpolicy.WorkPolicy;
import com.hermes.attendanceservice.entity.workpolicy.WorkCycle;
import com.hermes.attendanceservice.repository.workpolicy.WorkPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkPolicyServiceImpl implements WorkPolicyService {
    
    private final WorkPolicyRepository workPolicyRepository;
    private final AnnualLeaveService annualLeaveService;
    
    @Override
    public WorkPolicyResponseDto createWorkPolicy(WorkPolicyRequestDto requestDto) {
        log.info("근무 정책 생성 시작: {}", requestDto.getName());
        
        if (workPolicyRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 근무 정책 이름입니다: " + requestDto.getName());
        }
        
        validateFlexibleWorkSettings(requestDto);
        validateAndComputeBreakMinutes(requestDto);
        long breakMinutes = Duration.between(requestDto.getBreakStartTime(), requestDto.getBreakEndTime()).toMinutes();
        
        WorkPolicy workPolicy = WorkPolicy.builder()
                .name(requestDto.getName())
                .type(requestDto.getType())
                .workCycle(requestDto.getWorkCycle())
                .startDayOfWeek(requestDto.getStartDayOfWeek())
                .workCycleStartDay(requestDto.getWorkCycleStartDay())
                .workDays(requestDto.getWorkDays())
                .weeklyWorkingDays(requestDto.getWeeklyWorkingDays())
                .startTime(requestDto.getStartTime())
                .startTimeEnd(requestDto.getStartTimeEnd())
                .endTime(requestDto.getEndTime())
                .workHours(requestDto.getWorkHours())
                .workMinutes(requestDto.getWorkMinutes())
                .coreTimeStart(requestDto.getCoreTimeStart())
                .coreTimeEnd(requestDto.getCoreTimeEnd())
                .breakStartTime(requestDto.getBreakStartTime())
                .breakEndTime(requestDto.getBreakEndTime())
                .breakMinutes((int) breakMinutes)
                .avgWorkTime(requestDto.getAvgWorkTime())
                .totalRequiredMinutes(requestDto.getTotalRequiredMinutes())
                .build();
        
        WorkPolicy savedPolicy = workPolicyRepository.save(workPolicy);
        log.info("근무 정책 생성 완료: ID={}, 이름={}", savedPolicy.getId(), savedPolicy.getName());
        
        if (requestDto.getAnnualLeaves() != null && !requestDto.getAnnualLeaves().isEmpty()) {
            for (AnnualLeaveRequestDto annualLeaveDto : requestDto.getAnnualLeaves()) {
                annualLeaveService.createAnnualLeave(savedPolicy.getId(), annualLeaveDto);
            }
        }
        
        return convertToResponseDto(savedPolicy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public WorkPolicyResponseDto getWorkPolicyById(Long id) {
        log.info("근무 정책 조회 시작: ID={}", id);
        
        WorkPolicy workPolicy = workPolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근무 정책입니다: " + id));
        
        log.info("근무 정책 조회 완료: ID={}, 이름={}", id, workPolicy.getName());
        return convertToResponseDto(workPolicy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public WorkPolicyResponseDto getWorkPolicyByName(String name) {
        log.info("근무 정책 조회 시작: 이름={}", name);
        
        WorkPolicy workPolicy = workPolicyRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근무 정책입니다: " + name));
        
        log.info("근무 정책 조회 완료: 이름={}, ID={}", name, workPolicy.getId());
        return convertToResponseDto(workPolicy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<WorkPolicyListResponseDto> getWorkPolicyList(WorkPolicySearchDto searchDto) {
        log.info("근무 정책 목록 조회 시작: {}", searchDto);
        
        Sort sort = Sort.by(
                searchDto.getSortDirection().equalsIgnoreCase("DESC") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, 
                searchDto.getSortBy()
        );
        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize(), sort);
        
        Page<WorkPolicy> workPolicyPage = workPolicyRepository.findBySearchConditions(
                searchDto.getName(),
                searchDto.getType(),
                searchDto.getIsCompliantWithLaborLaw(),
                pageable
        );
        
        Page<WorkPolicyListResponseDto> responsePage = workPolicyPage.map(this::convertToListResponseDto);
        log.info("근무 정책 목록 조회 완료: 총 {}건", responsePage.getTotalElements());
        return responsePage;
    }
    
    @Override
    public WorkPolicyResponseDto updateWorkPolicy(Long id, WorkPolicyUpdateDto updateDto) {
        log.info("근무 정책 수정 시작: ID={}", id);
        
        WorkPolicy workPolicy = workPolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근무 정책입니다: " + id));
        
        validateFlexibleWorkSettingsForUpdate(workPolicy, updateDto);
        validateAndComputeBreakMinutes(updateDto, workPolicy);
        
        if (updateDto.getName() != null) {
            workPolicy.setName(updateDto.getName());
        }
        if (updateDto.getType() != null) {
            workPolicy.setType(updateDto.getType());
        }
        if (updateDto.getWorkCycle() != null) {
            workPolicy.setWorkCycle(updateDto.getWorkCycle());
        }
        if (updateDto.getStartDayOfWeek() != null) {
            workPolicy.setStartDayOfWeek(updateDto.getStartDayOfWeek());
        }
        if (updateDto.getWorkCycleStartDay() != null) {
            workPolicy.setWorkCycleStartDay(updateDto.getWorkCycleStartDay());
        }
        if (updateDto.getWorkDays() != null) {
            workPolicy.setWorkDays(updateDto.getWorkDays());
        }
        if (updateDto.getWeeklyWorkingDays() != null) {
            workPolicy.setWeeklyWorkingDays(updateDto.getWeeklyWorkingDays());
        }
        if (updateDto.getStartTime() != null) {
            workPolicy.setStartTime(updateDto.getStartTime());
        }
        if (updateDto.getStartTimeEnd() != null) {
            workPolicy.setStartTimeEnd(updateDto.getStartTimeEnd());
        }
        if (updateDto.getEndTime() != null) {
            workPolicy.setEndTime(updateDto.getEndTime());
        }
        if (updateDto.getWorkHours() != null) {
            workPolicy.setWorkHours(updateDto.getWorkHours());
        }
        if (updateDto.getWorkMinutes() != null) {
            workPolicy.setWorkMinutes(updateDto.getWorkMinutes());
        }
        if (updateDto.getCoreTimeStart() != null) {
            workPolicy.setCoreTimeStart(updateDto.getCoreTimeStart());
        }
        if (updateDto.getCoreTimeEnd() != null) {
            workPolicy.setCoreTimeEnd(updateDto.getCoreTimeEnd());
        }
        if (updateDto.getBreakStartTime() != null) {
            workPolicy.setBreakStartTime(updateDto.getBreakStartTime());
        }
        if (updateDto.getBreakEndTime() != null) {
            workPolicy.setBreakEndTime(updateDto.getBreakEndTime());
        }
        if (updateDto.getAvgWorkTime() != null) {
            workPolicy.setAvgWorkTime(updateDto.getAvgWorkTime());
        }
        if (updateDto.getTotalRequiredMinutes() != null) {
            workPolicy.setTotalRequiredMinutes(updateDto.getTotalRequiredMinutes());
        }
        
        WorkPolicy updatedPolicy = workPolicyRepository.save(workPolicy);
        log.info("근무 정책 수정 완료: ID={}, 이름={}", id, updatedPolicy.getName());
        
        return convertToResponseDto(updatedPolicy);
    }
    
    @Override
    public void deleteWorkPolicy(Long id) {
        log.info("근무 정책 삭제 시작: ID={}", id);
        WorkPolicy workPolicy = workPolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근무 정책입니다: " + id));
        workPolicyRepository.delete(workPolicy);
        log.info("근무 정책 삭제 완료: ID={}, 이름={}", id, workPolicy.getName());
    }
    
    @Override
    public boolean checkLaborLawCompliance(WorkPolicyRequestDto requestDto) {
        log.info("노동법 준수 여부 확인 시작: {}", requestDto.getName());
        WorkCycle workCycle = requestDto.getWorkCycle();
        Integer totalRequiredMinutes = requestDto.getTotalRequiredMinutes();
        boolean isCompliant;
        if (workCycle == WorkCycle.ONE_MONTH) {
            isCompliant = totalRequiredMinutes <= 9600;
        } else {
            isCompliant = totalRequiredMinutes <= 2400;
        }
        log.info("노동법 준수 여부 확인 완료: {} ({}분 {}기준)", 
                isCompliant, totalRequiredMinutes, workCycle);
        return isCompliant;
    }
    
    /** FLEXIBLE 검증 (생성 시) */
    private void validateFlexibleWorkSettings(WorkPolicyRequestDto requestDto) {
        if (requestDto.getType() == com.hermes.attendanceservice.entity.workpolicy.WorkType.FLEXIBLE) {
            if (requestDto.getStartTime() == null) {
                throw new IllegalArgumentException("시차 근무에서는 출근 시작 시간(startTime)이 필수입니다.");
            }
            if (requestDto.getStartTimeEnd() == null) {
                throw new IllegalArgumentException("시차 근무에서는 출근 종료 시간(startTimeEnd)이 필수입니다.");
            }
            if (requestDto.getStartTime().isAfter(requestDto.getStartTimeEnd()) || 
                requestDto.getStartTime().equals(requestDto.getStartTimeEnd())) {
                throw new IllegalArgumentException("시차 근무에서 출근 시작 시간은 출근 종료 시간보다 이전이어야 합니다.");
            }
        }
    }
    
    /** FLEXIBLE 검증 (수정 시) */
    private void validateFlexibleWorkSettingsForUpdate(WorkPolicy workPolicy, WorkPolicyUpdateDto updateDto) {
        if (updateDto.getType() == com.hermes.attendanceservice.entity.workpolicy.WorkType.FLEXIBLE || 
            workPolicy.getType() == com.hermes.attendanceservice.entity.workpolicy.WorkType.FLEXIBLE) {
            LocalTime startTime = updateDto.getStartTime() != null ? updateDto.getStartTime() : workPolicy.getStartTime();
            LocalTime startTimeEnd = updateDto.getStartTimeEnd() != null ? updateDto.getStartTimeEnd() : workPolicy.getStartTimeEnd();
            if (startTime == null) {
                throw new IllegalArgumentException("시차 근무에서는 출근 시작 시간(startTime)이 필수입니다.");
            }
            if (startTimeEnd == null) {
                throw new IllegalArgumentException("시차 근무에서는 출근 종료 시간(startTimeEnd)이 필수입니다.");
            }
            if (startTime.isAfter(startTimeEnd) || startTime.equals(startTimeEnd)) {
                throw new IllegalArgumentException("시차 근무에서 출근 시작 시간은 출근 종료 시간보다 이전이어야 합니다.");
            }
        }
    }
    
    /** 휴게 시간 검증 및 계산 (생성) */
    private void validateAndComputeBreakMinutes(WorkPolicyRequestDto requestDto) {
        if (requestDto.getBreakStartTime() == null || requestDto.getBreakEndTime() == null) {
            throw new IllegalArgumentException("휴게 시작/종료 시간은 모두 필수입니다.");
        }
        if (!requestDto.getBreakEndTime().isAfter(requestDto.getBreakStartTime())) {
            throw new IllegalArgumentException("휴게 종료 시간은 시작 시간보다 이후여야 합니다.");
        }
    }
    
    /** 휴게 시간 검증 및 계산 (수정) */
    private void validateAndComputeBreakMinutes(WorkPolicyUpdateDto updateDto, WorkPolicy current) {
        LocalTime start = updateDto.getBreakStartTime() != null ? updateDto.getBreakStartTime() : current.getBreakStartTime();
        LocalTime end = updateDto.getBreakEndTime() != null ? updateDto.getBreakEndTime() : current.getBreakEndTime();
        if (start == null || end == null) {
            return;
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("휴게 종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        long minutes = Duration.between(start, end).toMinutes();
        current.setBreakMinutes((int) minutes);
    }
    
    private WorkPolicyResponseDto convertToResponseDto(WorkPolicy workPolicy) {
        return WorkPolicyResponseDto.builder()
                .id(workPolicy.getId())
                .name(workPolicy.getName())
                .type(workPolicy.getType())
                .workCycle(workPolicy.getWorkCycle())
                .startDayOfWeek(workPolicy.getStartDayOfWeek())
                .workCycleStartDay(workPolicy.getWorkCycleStartDay())
                .workDays(workPolicy.getWorkDays())
                .weeklyWorkingDays(workPolicy.getWeeklyWorkingDays())
                .startTime(workPolicy.getStartTime())
                .startTimeEnd(workPolicy.getStartTimeEnd())
                .endTime(workPolicy.getEndTime())
                .workHours(workPolicy.getWorkHours())
                .workMinutes(workPolicy.getWorkMinutes())
                .coreTimeStart(workPolicy.getCoreTimeStart())
                .coreTimeEnd(workPolicy.getCoreTimeEnd())
                .breakStartTime(workPolicy.getBreakStartTime())
                .breakEndTime(workPolicy.getBreakEndTime())
                .breakMinutes(workPolicy.getBreakMinutes())
                .avgWorkTime(workPolicy.getAvgWorkTime())
                .totalRequiredMinutes(workPolicy.getTotalRequiredMinutes())
                .annualLeaves(annualLeaveService.getAnnualLeavesByWorkPolicyId(workPolicy.getId()))
                .createdAt(workPolicy.getCreatedAt())
                .updatedAt(workPolicy.getUpdatedAt())
                .totalWorkMinutes(workPolicy.getTotalWorkMinutes())
                .isCompliantWithLaborLaw(workPolicy.isCompliantWithLaborLaw())
                .isOptionalWork(workPolicy.isOptionalWork())
                .isShiftWork(workPolicy.isShiftWork())
                .isFlexibleWork(workPolicy.isFlexibleWork())
                .isFixedWork(workPolicy.isFixedWork())
                .build();
    }
    
    private WorkPolicyListResponseDto convertToListResponseDto(WorkPolicy workPolicy) {
        return WorkPolicyListResponseDto.builder()
                .id(workPolicy.getId())
                .name(workPolicy.getName())
                .type(workPolicy.getType())
                .workHours(workPolicy.getWorkHours())
                .workMinutes(workPolicy.getWorkMinutes())
                .totalRequiredMinutes(workPolicy.getTotalRequiredMinutes())
                .totalAnnualLeaveDays(annualLeaveService.calculateTotalLeaveDays(workPolicy.getId()))
                .totalHolidayDays(annualLeaveService.calculateTotalHolidayDays(workPolicy.getId()))
                .createdAt(workPolicy.getCreatedAt())
                .updatedAt(workPolicy.getUpdatedAt())
                .totalWorkMinutes(workPolicy.getTotalWorkMinutes())
                .isCompliantWithLaborLaw(workPolicy.isCompliantWithLaborLaw())
                .build();
    }
} 