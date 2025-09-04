package com.hermes.attendanceservice.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    private Long userId;              // 출근하는 사용자의 ID
    private Instant checkIn;          // 출근 시간 (보통 서버에서 now()로 처리 가능)
} 
