package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.leave.CreateLeaveRequestDto;
import com.hermes.attendanceservice.dto.leave.LeaveRequestResponseDto;
import com.hermes.attendanceservice.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {
    
    private final LeaveService leaveService;
    
    /**
     * 휴가 신청을 생성합니다.
     * @param createDto 휴가 신청 생성 DTO
     * @return 생성된 휴가 신청 응답
     */
    @PostMapping
    public ApiResult<LeaveRequestResponseDto> createLeaveRequest(
            @Valid @RequestBody CreateLeaveRequestDto createDto) {
        try {
            log.info("휴가 신청 생성 요청: employeeId={}, leaveType={}, startDate={}, endDate={}", 
                    createDto.getEmployeeId(), createDto.getLeaveType(), 
                    createDto.getStartDate(), createDto.getEndDate());
            
            LeaveRequestResponseDto response = leaveService.createLeaveRequest(createDto);
            
            return ApiResult.success("휴가 신청이 성공적으로 생성되었습니다.", response);
                    
        } catch (RuntimeException e) {
            log.error("휴가 신청 생성 실패: {}", e.getMessage());
            return ApiResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("휴가 신청 생성 예외 발생: ", e);
            return ApiResult.failure("휴가 신청 생성 중 예외가 발생했습니다.");
        }
    }
    
    /**
     * 휴가 신청을 수정합니다.(기존 신청 삭제 후 새로 생성)
     * @param requestId 기존 휴가 신청 ID
     * @param createDto 새로운 휴가 신청 내용
     * @return 수정된 휴가 신청 응답
     */
    @PutMapping("/{requestId}")
    public ApiResult<LeaveRequestResponseDto> modifyLeaveRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateLeaveRequestDto createDto) {
        try {
            log.info("휴가 신청 수정 요청: requestId={}, employeeId={}, leaveType={}", 
                    requestId, createDto.getEmployeeId(), createDto.getLeaveType());
            
            LeaveRequestResponseDto response = leaveService.modifyLeaveRequest(requestId, createDto);
            
            return ApiResult.success("휴가 신청이 성공적으로 수정되었습니다.", response);
                    
        } catch (RuntimeException e) {
            log.error("휴가 신청 수정 실패: {}", e.getMessage());
            return ApiResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("휴가 신청 수정 예외 발생: ", e);
            return ApiResult.failure("휴가 신청 수정 중 예외가 발생했습니다.");
        }
    }
    
    /**
     * 휴가 신청을 조회합니다.
     * @param requestId 휴가 신청 ID
     * @return 휴가 신청 상세 정보
     */
    @GetMapping("/{requestId}")
    public ApiResult<LeaveRequestResponseDto> getLeaveRequest(
            @PathVariable Long requestId) {
        try {
            log.info("휴가 신청 조회 요청: requestId={}", requestId);
            
            LeaveRequestResponseDto response = leaveService.getLeaveRequest(requestId);
            
            if (response == null) {
                return ApiResult.failure("휴가 신청을 찾을 수 없습니다.");
            }
            
            return ApiResult.success("휴가 신청 조회가 완료되었습니다.", response);
                    
        } catch (RuntimeException e) {
            log.error("휴가 신청 조회 실패: {}", e.getMessage());
            return ApiResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("휴가 신청 조회 예외 발생: ", e);
            return ApiResult.failure("휴가 신청 조회 중 예외가 발생했습니다.");
        }
    }
} 
