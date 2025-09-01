package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.workpolicy.WorkPolicyRequestDto;
import com.hermes.attendanceservice.dto.workpolicy.WorkPolicyResponseDto;
import com.hermes.attendanceservice.entity.workpolicy.WorkPolicy;
import com.hermes.attendanceservice.entity.workpolicy.WorkType;
import com.hermes.attendanceservice.repository.workpolicy.WorkPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workpolicy")
public class WorkPolicyController {
    
    private final WorkPolicyRepository workPolicyRepository;
    
    /**
     * 근무 정책 생성
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<WorkPolicyResponseDto> createWorkPolicy(@Valid @RequestBody WorkPolicyRequestDto requestDto) {
        try {
            log.info("Creating work policy: {}", requestDto.getName());
            
            // OPTIONAL 타입일 때 코어 타임 검증
            if (requestDto.getType() == WorkType.OPTIONAL) {
                if (requestDto.getCoreTimeStart() == null || requestDto.getCoreTimeEnd() == null) {
                    return ApiResult.failure("선택 근무(OPTIONAL) 타입은 코어 타임 시작/종료 시간이 필수입니다.");
                }
                if (requestDto.getCoreTimeStart().isAfter(requestDto.getCoreTimeEnd())) {
                    return ApiResult.failure("코어 타임 시작 시간은 종료 시간보다 빨라야 합니다.");
                }
            }
            
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
                    .workHours(requestDto.getWorkHours())
                    .workMinutes(requestDto.getWorkMinutes())
                    .coreTimeStart(requestDto.getCoreTimeStart())
                    .coreTimeEnd(requestDto.getCoreTimeEnd())
                    .breakStartTime(requestDto.getBreakStartTime())
                    .avgWorkTime(requestDto.getAvgWorkTime())
                    .totalRequiredMinutes(requestDto.getTotalRequiredMinutes())
                    .build();
            
            WorkPolicy savedWorkPolicy = workPolicyRepository.save(workPolicy);
            WorkPolicyResponseDto response = WorkPolicyResponseDto.from(savedWorkPolicy);
            
            return ApiResult.success("근무 정책이 성공적으로 생성되었습니다.", response);
            
        } catch (Exception e) {
            log.error("Error creating work policy: {}", requestDto.getName(), e);
            return ApiResult.failure("근무 정책 생성에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 전체 근무 정책 목록 조회
     */
    @GetMapping
    public ApiResult<List<WorkPolicyResponseDto>> getAllWorkPolicies() {
        try {
            log.info("Get all work policies");
            
            List<WorkPolicy> workPolicies = workPolicyRepository.findAll();
            List<WorkPolicyResponseDto> responses = workPolicies.stream()
                    .map(WorkPolicyResponseDto::from)
                    .toList();
            
            return ApiResult.success("전체 근무 정책 목록을 성공적으로 조회했습니다.", responses);
            
        } catch (Exception e) {
            log.error("Error getting all work policies", e);
            return ApiResult.failure("근무 정책 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * WorkPolicy ID로 근무 정책 정보 조회
     */
    @GetMapping("/{workPolicyId}")
    public ApiResult<WorkPolicyResponseDto> getWorkPolicyById(@PathVariable Long workPolicyId) {
        try {
            log.info("Get work policy by id: {}", workPolicyId);
            
            WorkPolicy workPolicy = workPolicyRepository.findById(workPolicyId)
                    .orElse(null);
            
            if (workPolicy == null) {
                return ApiResult.failure("근무 정책을 찾을 수 없습니다: " + workPolicyId);
            }
            
            WorkPolicyResponseDto response = WorkPolicyResponseDto.from(workPolicy);
            return ApiResult.success("근무 정책 정보를 성공적으로 조회했습니다.", response);
            
        } catch (Exception e) {
            log.error("Error getting work policy by id: {}", workPolicyId, e);
            return ApiResult.failure("근무 정책 조회에 실패했습니다: " + e.getMessage());
        }
    }
} 
