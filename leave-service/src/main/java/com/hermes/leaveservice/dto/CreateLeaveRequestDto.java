package com.hermes.leaveservice.dto;

import com.hermes.leaveservice.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequestDto {
    
    @NotNull(message = "직원 ID는 필수입니다")
    private Long employeeId;
    
    @NotNull(message = "휴가 유형은 필수입니다")
    private LeaveType leaveType;
    
    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;
    
    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    @NotBlank(message = "휴가 사유는 필수입니다")
    private String reason;
} 