package com.hermes.attendanceservice.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyWorkBase {
    private Long userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart; // 주의 시작일(일요일)
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnd; // 주의 종료일(토요일)
} 
