package com.hermes.attendanceservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.dto.leave.CreateLeaveRequestDto;
import com.hermes.attendanceservice.dto.leave.LeaveRequestResponseDto;
import com.hermes.attendanceservice.service.leave.LeaveService;
import com.hermes.auth.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave", description = "휴가 신청 관리 API")
public class LeaveController {
    
    private final LeaveService leaveService;
    
    @Operation(summary = "휴가 신청 생성", description = "새로운 휴가 신청을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "휴가 신청 생성 성공",
            content = @Content(schema = @Schema(implementation = LeaveRequestResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    public ApiResult<LeaveRequestResponseDto> createLeaveRequest(
            @Parameter(description = "휴가 신청 정보") @Valid @RequestBody CreateLeaveRequestDto createDto,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            // 본인만 휴가 신청 가능
            if (!user.getUserId().equals(createDto.getEmployeeId())) {
                return ApiResult.failure("권한이 없습니다.");
            }
            
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
    
    @Operation(summary = "휴가 신청 수정", description = "기존 휴가 신청을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "휴가 신청 수정 성공",
            content = @Content(schema = @Schema(implementation = LeaveRequestResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "휴가 신청을 찾을 수 없음")
    })
    @PutMapping("/{requestId}")
    @PreAuthorize("hasRole('ADMIN') or #createDto.employeeId == authentication.principal.userId")
    public ApiResult<LeaveRequestResponseDto> modifyLeaveRequest(
            @Parameter(description = "휴가 신청 ID") @PathVariable Long requestId,
            @Parameter(description = "수정할 휴가 신청 정보") @Valid @RequestBody CreateLeaveRequestDto createDto) {
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
    
    @Operation(summary = "휴가 신청 조회", description = "특정 휴가 신청의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "휴가 신청 조회 성공",
            content = @Content(schema = @Schema(implementation = LeaveRequestResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "휴가 신청을 찾을 수 없음")
    })
    @GetMapping("/{requestId}")
    public ApiResult<LeaveRequestResponseDto> getLeaveRequest(
            @Parameter(description = "휴가 신청 ID") @PathVariable Long requestId,
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal UserPrincipal user) {
        try {
            log.info("휴가 신청 조회 요청: requestId={}", requestId);
            
            LeaveRequestResponseDto response = leaveService.getLeaveRequest(requestId);
            
            if (response == null) {
                return ApiResult.failure("휴가 신청을 찾을 수 없습니다.");
            }
            
            // 본인 또는 관리자만 조회 가능
            if (!user.getUserId().equals(response.getEmployeeId()) && !user.getRole().name().equals("ADMIN")) {
                return ApiResult.failure("권한이 없습니다.");
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
