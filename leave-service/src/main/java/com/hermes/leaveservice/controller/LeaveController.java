package com.hermes.leaveservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.leaveservice.dto.CreateLeaveRequestDto;
import com.hermes.leaveservice.dto.LeaveRequestResponseDto;
import com.hermes.leaveservice.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * 휴가 신청을 생성합니다
     * @param createDto 휴가 신청 생성 DTO
     * @return 생성된 휴가 신청 응답
     */
    @PostMapping
    public ResponseEntity<ApiResult<LeaveRequestResponseDto>> createLeaveRequest(
            @Valid @RequestBody CreateLeaveRequestDto createDto) {
        try {
            log.info("휴가 신청 생성 요청: employeeId={}, leaveType={}, startDate={}, endDate={}", 
                    createDto.getEmployeeId(), createDto.getLeaveType(), 
                    createDto.getStartDate(), createDto.getEndDate());
            
            LeaveRequestResponseDto response = leaveService.createLeaveRequest(createDto);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResult.success("휴가 신청이 성공적으로 생성되었습니다.", response));
                    
        } catch (RuntimeException e) {
            log.error("휴가 신청 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("휴가 신청 생성 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("휴가 신청 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 휴가 신청을 수정합니다 (삭제 후 재생성)
     * @param requestId 기존 휴가 신청 ID
     * @param createDto 새로운 휴가 신청 데이터
     * @return 수정된 휴가 신청 응답
     */
    @PutMapping("/{requestId}")
    public ResponseEntity<ApiResult<LeaveRequestResponseDto>> modifyLeaveRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateLeaveRequestDto createDto) {
        try {
            log.info("휴가 신청 수정 요청: requestId={}, employeeId={}, leaveType={}", 
                    requestId, createDto.getEmployeeId(), createDto.getLeaveType());
            
            LeaveRequestResponseDto response = leaveService.modifyLeaveRequest(requestId, createDto);
            
            return ResponseEntity.ok()
                    .body(ApiResult.success("휴가 신청이 성공적으로 수정되었습니다.", response));
                    
        } catch (RuntimeException e) {
            log.error("휴가 신청 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("휴가 신청 수정 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("휴가 신청 수정 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 휴가 신청을 조회합니다
     * @param requestId 휴가 신청 ID
     * @return 휴가 신청 상세 정보
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResult<LeaveRequestResponseDto>> getLeaveRequest(
            @PathVariable Long requestId) {
        try {
            log.info("휴가 신청 조회 요청: requestId={}", requestId);
            
            LeaveRequestResponseDto response = leaveService.getLeaveRequest(requestId);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .body(ApiResult.success("휴가 신청 조회가 완료되었습니다.", response));
                    
        } catch (RuntimeException e) {
            log.error("휴가 신청 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("휴가 신청 조회 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("휴가 신청 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 직원의 휴가 신청 목록을 조회합니다
     * @param employeeId 직원 ID
     * @return 휴가 신청 목록
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResult<List<LeaveRequestResponseDto>>> getLeaveRequestsByEmployee(
            @PathVariable Long employeeId) {
        try {
            log.info("직원 휴가 신청 목록 조회 요청: employeeId={}", employeeId);
            
            List<LeaveRequestResponseDto> response = leaveService.getLeaveRequestsByEmployee(employeeId);
            
            if (response == null) {
                return ResponseEntity.ok()
                        .body(ApiResult.success("휴가 신청 내역이 없습니다.", List.of()));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResult.success("직원 휴가 신청 목록 조회가 완료되었습니다.", response));
                    
        } catch (RuntimeException e) {
            log.error("직원 휴가 신청 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("직원 휴가 신청 목록 조회 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("직원 휴가 신청 목록 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 휴가 신청을 삭제합니다
     * @param requestId 휴가 신청 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResult<Void>> deleteLeaveRequest(
            @PathVariable Long requestId) {
        try {
            log.info("휴가 신청 삭제 요청: requestId={}", requestId);
            
            leaveService.deleteLeaveRequest(requestId);
            
            return ResponseEntity.ok()
                    .body(ApiResult.success("휴가 신청이 성공적으로 삭제되었습니다."));
                    
        } catch (RuntimeException e) {
            log.error("휴가 신청 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("휴가 신청 삭제 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("휴가 신청 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 휴가 시간을 계산합니다
     * @param createDto 휴가 신청 데이터
     * @return 계산된 휴가 시간
     */
    @PostMapping("/calculate-hours")
    public ResponseEntity<ApiResult<Double>> calculateTotalHours(
            @Valid @RequestBody CreateLeaveRequestDto createDto) {
        try {
            log.info("휴가 시간 계산 요청: startDate={}, endDate={}, startTime={}, endTime={}", 
                    createDto.getStartDate(), createDto.getEndDate(), 
                    createDto.getStartTime(), createDto.getEndTime());
            
            double totalHours = leaveService.calculateTotalHours(createDto);
            
            return ResponseEntity.ok()
                    .body(ApiResult.success("휴가 시간 계산이 완료되었습니다.", totalHours));
                    
        } catch (RuntimeException e) {
            log.error("휴가 시간 계산 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResult.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("휴가 시간 계산 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.failure("휴가 시간 계산 중 오류가 발생했습니다."));
        }
    }
} 