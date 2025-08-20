package com.hermes.attendanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    private Long userId;              // 출근하는 사용자 ID
    private LocalDateTime checkIn;    // 출근 시간 (보통 서버에서 now()로 처리 가능)
}
