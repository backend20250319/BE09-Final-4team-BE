package com.hermes.attendanceservice.dto.leave;

import com.hermes.attendanceservice.entity.leave.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponseDto {
    
    private Long requestId;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double totalDays;
    private Double totalHours;
    private String reason;
    private String status;
    private String statusName;
    private Long approverId;
    private String approverName;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
} 
