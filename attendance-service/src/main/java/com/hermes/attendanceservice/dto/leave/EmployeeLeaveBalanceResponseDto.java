package com.hermes.attendanceservice.dto.leave;

import com.hermes.attendanceservice.entity.leave.LeaveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "직원 연차 잔액 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLeaveBalanceResponseDto {
    
    @Schema(description = "연차 잔액 ID", example = "1")
    private Long id;
    
    @Schema(description = "직원 ID", example = "123")
    private Long employeeId;
    
    @Schema(description = "연차 타입", example = "BASIC_ANNUAL")
    private LeaveType leaveType;
    
    @Schema(description = "연차 타입 한글명", example = "기본 연차")
    private String leaveTypeName;
    
    @Schema(description = "총 부여된 연차 일수", example = "15")
    private Integer totalLeaveDays;
    
    @Schema(description = "사용한 연차 일수", example = "3")
    private Integer usedLeaveDays;
    
    @Schema(description = "잔여 연차 일수", example = "12")
    private Integer remainingDays;
    
    @Schema(description = "근무년수", example = "2")
    private Integer workYears;
    
    @Schema(description = "연차 사용률 (0.0 ~ 1.0)", example = "0.2")
    private Double usageRate;
    
    @Schema(description = "생성일시")
    private Instant createdAt;
    
    @Schema(description = "수정일시")
    private Instant updatedAt;
} 