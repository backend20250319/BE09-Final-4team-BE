package com.hermes.attendanceservice.dto.leave;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Schema(description = "직원 연차 잔액 요약 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLeaveBalanceSummaryDto {
    
    @Schema(description = "직원 ID", example = "123")
    private Long employeeId;
    
    @Schema(description = "전체 잔여 연차 일수", example = "12")
    private Integer totalRemainingDays;
    
    @Schema(description = "전체 사용한 연차 일수", example = "3")
    private Integer totalUsedDays;
    
    @Schema(description = "전체 부여받은 연차 일수", example = "15")
    private Integer totalGrantedDays;
    
    @Schema(description = "연차 타입별 상세 정보")
    private List<EmployeeLeaveBalanceResponseDto> leaveBalances;
    
    @Schema(description = "기본 연차 잔여 일수", example = "10")
    private Integer basicAnnualRemaining;
    
    @Schema(description = "보상 연차 잔여 일수", example = "2")
    private Integer compensationAnnualRemaining;
    
    @Schema(description = "특별 연차 잔여 일수", example = "0")
    private Integer specialAnnualRemaining;
    
    @Schema(description = "전체 연차 사용률 (0.0 ~ 1.0)", example = "0.2")
    private Double overallUsageRate;
} 