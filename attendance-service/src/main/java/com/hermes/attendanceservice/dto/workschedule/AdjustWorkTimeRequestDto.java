package com.hermes.attendanceservice.dto.workschedule;

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
public class AdjustWorkTimeRequestDto {
    
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
    
    @NotNull(message = "조정 날짜는 필수입니다.")
    private LocalDate adjustDate;
    
    @NotNull(message = "조정 유형은 필수입니다.")
    private String adjustType; // EXTEND (연장), REDUCE (단축), FLEXIBLE (시차근무)
    
    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalTime startTime;
    
    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalTime endTime;
    
    @NotBlank(message = "조정 사유는 필수입니다.")
    private String reason;
    
    private String description;
} 